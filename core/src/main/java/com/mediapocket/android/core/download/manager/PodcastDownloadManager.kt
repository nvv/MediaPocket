package com.mediapocket.android.core.download.manager

import android.content.Context
import com.mediapocket.android.core.download.extensions.isDownloading
import com.mediapocket.android.core.download.mapper.FetchToDownloadManagerErrorMapper
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.*


@ExperimentalCoroutinesApi
class PodcastDownloadManager(
        private val context: Context,
        private val repository: PodcastEpisodeRepository,
        private val errorMapper: FetchToDownloadManagerErrorMapper
) {

    /**
     * All active downloading items, including manually paused or paused due to network error.
     */
    private val downloadingItems = mutableMapOf<String, PodcastDownloadItem>()

    /**
     * Channel to track all currently running downloads. Paused downloads won't be included in the channel.
     * "Subscribe" on this channel to receive for downloads updates.
     */
    val activeDownloads = BroadcastChannel<List<PodcastDownloadItem>>(Channel.CONFLATED)

    /**
     * Channel to track all 'downloading' items including paused.
     * "Subscribe" on this channel to receive for downloads updates.
     */
    val downloads = BroadcastChannel<List<PodcastDownloadItem>>(Channel.CONFLATED)

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
                        item.downloadDate = System.currentTimeMillis()
                        repository.update(item)
                    }

                    fetch.remove(download.id)
                    val downloadingItem = downloadingItems.remove(id)
                        downloadingItem?.let {
                            downloadingItem.progress = 100
                            downloadingItem.state = PodcastEpisodeItem.STATE_DOWNLOADED
                            downloadingItem.isDownloaded = true
                        }

                    notifyActiveDownloadsChanged()
                }
            }

            override fun onDeleted(download: Download) {

            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                GlobalScope.launch {
                    val id = PodcastEpisodeItem.convertLinkToId(download.url)
                    val item = repository.get(id)
                    item?.let {
                        item.state = PodcastEpisodeItem.STATE_NONE
                        repository.update(item)
                    }

                    fetch.remove(download.id)
                    val downloadingItem = downloadingItems.remove(id)
                        downloadingItem?.let {
                            downloadingItem.progress = 0
                            downloadingItem.isDownloaded = false
                            downloadingItem.state = PodcastEpisodeItem.STATE_ERROR
                            downloadingItem.error = errorMapper.map(error)
                        }

                    notifyActiveDownloadsChanged()
                }
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
                    item?.let {
                        func(item)
                        GlobalScope.launch {
                            notifyActiveDownloadsChanged()
                        }
                }
            }
        })
    }

    fun download(item: PodcastEpisodeItem) {

        GlobalScope.launch {
            item.link?.let {
                val link = item.link ?: ""
                val file = getItemLocalPath(item.podcastId ?: "_")

                val request = Request(link, file)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL

                val id = PodcastEpisodeItem.convertLinkToId(item.link)
                var storedItem = repository.get(id)

                if (storedItem == null) {
                    storedItem = item
                    storedItem.state = PodcastEpisodeItem.STATE_DOWNLOADING
                    storedItem.localPath = file
                    storedItem.downloadId = request.id

                    repository.insert(storedItem)
                } else {
                    storedItem.state = PodcastEpisodeItem.STATE_DOWNLOADING
                    storedItem.localPath = file
                    storedItem.downloadId = request.id
                    repository.update(storedItem)
                }

                val downloadItem = PodcastDownloadItem(storedItem.id, storedItem.state, 0,
                        false, storedItem.title, request.id, storedItem.podcastId ?: "")
                downloadingItems[storedItem.id] = downloadItem

                fetch.enqueue(request)

                notifyActiveDownloadsChanged()
            }
        }
    }

    fun pauseDownload(episodeId: String) {
        downloadingItems[episodeId]?.let { download ->
            fetch.pause(download.downloadId)
        }
    }

    fun resumeDownload(episodeId: String) {
        downloadingItems[episodeId]?.let { download ->
            fetch.resume(download.downloadId)
        }
    }

    private suspend fun notifyActiveDownloadsChanged() {
        activeDownloads.send(downloadingItems.values.filter { it.isDownloading }.toList())
        downloads.send(downloadingItems.values.toList())
    }

    private fun getItemLocalPath(podcastId: String) = context.filesDir.absolutePath + "/podcast_items/" + podcastId + "/" + UUID.randomUUID().toString()

}