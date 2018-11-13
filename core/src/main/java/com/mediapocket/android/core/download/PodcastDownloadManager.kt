package com.mediapocket.android.core.download

import android.content.Context
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.DownloadedPodcastItem
import com.mediapocket.android.model.Item
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * @author Vlad Namashko
 */
class PodcastDownloadManager(private val context: Context, private val database: AppDatabase) {

    private var fetch: Fetch

    private val databaseSubject = BehaviorSubject.create<List<PodcastDownloadItem>>()
    private val downloadsSubject = BehaviorSubject.create<PodcastDownloadItem>()

    private val downloadingItems = mutableMapOf<String, PodcastDownloadItem>()

    init {
        val fetchConfiguration = FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(4)
                .build()

        Completable.fromAction {
            val items = getStoredItems()
            items?.filter { it -> it.state != DownloadedPodcastItem.STATE_DOWNLOADED }?.let {
                it.forEach { i -> downloadingItems[i.id] = i }
            }

            databaseSubject.onNext(items ?: emptyList())
        }.subscribeOn(Schedulers.io()).subscribe()

        fetch = Fetch.getInstance(fetchConfiguration)

        fetch.addListener(object : FetchListener {
            override fun onAdded(download: Download) {
                val item = downloadingItems[DownloadedPodcastItem.convertLinkToId(download.url)]
                item?.let {
                    it.state = DownloadedPodcastItem.STATE_ADDED
                    downloadsSubject.onNext(it)
                }
            }

            override fun onCancelled(download: Download) {
                onItemChanged(download)
            }

            override fun onCompleted(download: Download) {
                onItemChanged(download)
                Completable.fromAction {
                    val dao = database.downloadedPodcastItemDao()
                    val item = dao.get(DownloadedPodcastItem.convertLinkToId(download.url))
                    item?.let {
                        item.state = DownloadedPodcastItem.STATE_DOWNLOADED
                        dao.update(item)

                        val downloadingItem = downloadingItems[item.id]
                        downloadingItem?.let {
                            downloadingItems.remove(it.id)
//                            it.state = DownloadedPodcastItem.STATE_DOWNLOADED
//                            it.progress = 100

                            //downloadsSubject.onNext(it)
                            databaseSubject.onNext(getStoredItemsWithProgress() ?: emptyList())
                        }
                    }

                }.subscribeOn(Schedulers.io()).subscribe()
            }

            override fun onDeleted(download: Download) {
                onItemChanged(download)
            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
                onItemChanged(download)
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                onItemChanged(download)
            }

            override fun onPaused(download: Download) {
                onItemChanged(download)
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                val item = downloadingItems[DownloadedPodcastItem.convertLinkToId(download.url)]
                item?.let {
                    it.state = DownloadedPodcastItem.STATE_DOWNLOADING
                    it.progress = download.progress
                    downloadsSubject.onNext(it)
                }
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                onItemChanged(download)
            }

            override fun onRemoved(download: Download) {
                onItemChanged(download)
            }

            override fun onResumed(download: Download) {
                onItemChanged(download)
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                onItemChanged(download)
            }

            override fun onWaitingNetwork(download: Download) {
                onItemChanged(download)
            }
        })
    }

    private fun getStoredItems () = database.downloadedPodcastItemDao().getAll()?.map { it ->
            PodcastDownloadItem(it.id, it.state, 0, it.podcastId, it.podcastTitle,
                    it.title, it.description, it.link, it.pubDate, it.length, it.imageUrl, it.localPath)
    }

    private fun getStoredItemsWithProgress() = getStoredItems()?.map { it ->
        downloadingItems[it.id]?.let { downloading ->
            it.state = downloading.state
            it.progress = downloading.progress
        }
        it
    }

    private fun onItemChanged(download: Download) {

    }

    fun subscribeForDownloads(consumer : Consumer<PodcastDownloadItem>) = downloadsSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer)

    fun subscribeForDatabase(consumer : Consumer<List<PodcastDownloadItem>>) =
            databaseSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer)

    fun download(podcastId: String?, item: Item): Observable<Int> {

        val observable : Observable<Int> = Observable.create { emmiter ->

            val file = DependencyLocator.getInstance().context.filesDir.absolutePath + "/podcast_items/" + podcastId + "/" + item.link?.replace("/", "$")
            val dao = DependencyLocator.getInstance().database.downloadedPodcastItemDao()

            val inserted = DownloadedPodcastItem(DownloadedPodcastItem.STATE_ADDED, podcastId,
                    item.podcastTitle, item.title, item.description, item.link, item.pubDate, item.length, item.imageUrl, file)
            dao.insert(inserted)

            val downloading = PodcastDownloadItem(inserted.id, inserted.state, 0, inserted.podcastId,
                    inserted.podcastTitle, inserted.title, inserted.description, inserted.link,
                    inserted.pubDate, inserted.length, inserted.imageUrl, inserted.localPath)
            downloadingItems[downloading.id] = downloading


            val request = Request(item.link!!, file)
            request.priority = Priority.HIGH
            request.networkType = NetworkType.ALL

            fetch.enqueue(request)


            databaseSubject.onNext(getStoredItemsWithProgress() ?: emptyList())

        }

        return observable.subscribeOn(Schedulers.io())
    }

    fun allDownloads() = DependencyLocator.getInstance().database.downloadedPodcastItemDao().getAll()
}