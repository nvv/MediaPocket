package com.mediapocket.android.core.download.manager

import android.content.Context
import com.mediapocket.android.core.download.extensions.isDownloading
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.*


@ExperimentalCoroutinesApi
class PodcastDownloadManager(
        private val context: Context,
        private val repository: PodcastEpisodeRepository) {

    /**
     * All active download broadcast channels to report download progress.
     * Add broadcast channel in this map and remove it after download has been completed.
     */
    private val downloadingChannels =
            mutableMapOf<String, BroadcastChannel<PodcastDownloadItem>>()

    /**
     * All active downloading items, including manually paused or paused due to network error.
     */
    private val downloadingItems = mutableMapOf<String, PodcastDownloadItem>()

    /**
     * Channel to track all currently running downloads. Paused downloads won't be included in the channel.
     * "Subscribe" on this channel to receive for downloads updates.
     */
    val activeDownloads = BroadcastChannel<List<PodcastDownloadItem>>(Channel.CONFLATED)

    private var fetch: Fetch = Fetch.getInstance(
            FetchConfiguration.Builder(context)
                    .setProgressReportingInterval(1000)
                    .setDownloadConcurrentLimit(4)
                    .build())

    init {
        fetch.addListener(object : FetchListener {
            override fun onAdded(download: Download) {

            }

            override fun onCancelled(download: Download) {

            }

            override fun onCompleted(download: Download) {
                GlobalScope.launch {
                    val id = PodcastEpisodeItem.convertLinkToId(download.url)
                    val item = repository.get(id)
                    item?.let {
                        item.state = PodcastEpisodeItem.STATE_DOWNLOADED
                        repository.update(item)
                    }

                    fetch.remove(download.id)
                    val downloadingItem = downloadingItems.remove(id)
                    downloadingChannels.remove(id)?.let { channel ->
                        downloadingItem?.let {
                            downloadingItem.progress = 100
                            downloadingItem.isDownloaded = true
                            channel.send(downloadingItem)
                        }
                        channel.close()
                    }

                    notifyActiveDownloadsChanged()
                }
            }

            override fun onDeleted(download: Download) {

            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {

            }

            override fun onPaused(download: Download) {
                onItemChanged(download) { item -> item.state = PodcastEpisodeItem.STATE_PAUSED }
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                onItemChanged(download) { item -> item.progress = download.progress }
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {

            }

            override fun onRemoved(download: Download) {

            }

            override fun onResumed(download: Download) {
                onItemChanged(download) { item -> item.state = PodcastEpisodeItem.STATE_DOWNLOADING }
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {

            }

            override fun onWaitingNetwork(download: Download) {

            }

            private fun onItemChanged(download: Download, func : (item: PodcastDownloadItem) -> Unit) {
                val id = PodcastEpisodeItem.convertLinkToId(download.url)
                val item = downloadingItems[id]
                downloadingChannels[id].let { channel ->
                    item?.let {
                        func(item)
                        GlobalScope.launch {
                            channel?.send(item)
                            notifyActiveDownloadsChanged()
                        }
                    }
                }
            }
        })
    }

    /**
     * Get currently active downloads for <code>podcastId</code>
     */
    fun getActiveDownloads(podcastId: String?): List<PodcastDownloadItem>? =
            downloadingItems.filter { it.value.podcastId == podcastId }.values.toList()

    /**
     * Request download progress channel.
     *
     * @param id download item item
     */
    fun listenForDownloadProgress(id: String): BroadcastChannel<PodcastDownloadItem>? = downloadingChannels[id]

    fun download(podcastId: String?, item: Item): BroadcastChannel<PodcastDownloadItem>? {
        val progress = BroadcastChannel<PodcastDownloadItem>(Channel.CONFLATED)
        downloadingChannels[PodcastEpisodeItem.convertLinkToId(item.link)] = progress

        GlobalScope.launch {
            item.link?.let {
                val file = getItemLocalPath(podcastId ?: "_", item.link)

                val request = Request(item.link, file)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL

                val id = PodcastEpisodeItem.convertLinkToId(item.link)
                var storedItem = repository.get(id)

                if (storedItem == null) {
                    storedItem = buildDatabaseItem(podcastId, item)
                    storedItem.state = PodcastEpisodeItem.STATE_DOWNLOADING
                    storedItem.localPath = file
                    storedItem.downloadId = request.id

                    repository.insert(storedItem)
                } else {
                    storedItem.state = PodcastEpisodeItem.STATE_DOWNLOADING
                    repository.update(storedItem)
                }

                val downloadItem = PodcastDownloadItem(storedItem.id, storedItem.state, 0,
                        false, storedItem.title, request.id, storedItem.podcastId ?: "")
                downloadingItems[storedItem.id] = downloadItem

                fetch.enqueue(request)

                progress.send(downloadItem)

                notifyActiveDownloadsChanged()
            }
        }

        return progress
    }

    fun pauseDownload(item: Item) {
        downloadingItems[PodcastEpisodeItem.convertLinkToId(item.link)]?.let { download ->
            fetch.pause(download.downloadId)
        }
    }

    fun resumeDownload(item: Item) {
        downloadingItems[PodcastEpisodeItem.convertLinkToId(item.link)]?.let { download ->
            fetch.resume(download.downloadId)
        }
    }

    private suspend fun notifyActiveDownloadsChanged() {
        activeDownloads.send(downloadingItems.values.filter { it.isDownloading }.toList())
    }

    private fun buildDatabaseItem(podcastId: String?, item: Item) =
            PodcastEpisodeItem(PodcastEpisodeItem.STATE_NONE, podcastId,
                    item.podcastTitle, item.title, item.description, item.link, System.currentTimeMillis(),
                    item.pubDate, item.length, false, item.imageUrl, 0, null)

    private fun getItemLocalPath(podcastId: String, link: String) = context.filesDir.absolutePath + "/podcast_items/" + podcastId + "/" + UUID.randomUUID().toString()

}