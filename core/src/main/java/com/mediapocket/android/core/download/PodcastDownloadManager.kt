package com.mediapocket.android.core.download

import android.content.Context
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2core.Func2
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastDownloadManager(context: Context) {

    private var fetch: Fetch

    private val databaseSubject = BehaviorSubject.create<List<PodcastDownloadItem>>()
    private val databaseChangesSubject = PublishSubject.create<List<PodcastDownloadItem>>()
    private val downloadsSubject = PublishSubject.create<PodcastDownloadItem>()
    private val allActiveDownloadsSubject = PublishSubject.create<List<PodcastDownloadItem>>()

    private val downloadingItems = mutableMapOf<String, PodcastDownloadItem>()

    @set:Inject
    lateinit var database: AppDatabase

    init {
        DependencyLocator.getInstance().coreComponent.inject(this)

        val fetchConfiguration = FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(4)
                .build()

        fetch = Fetch.getInstance(fetchConfiguration)

        fetch.addListener(object : FetchListener {
            override fun onAdded(download: Download) {
                onItemChanged(download)
                val item = downloadingItems[PodcastEpisodeItem.convertLinkToId(download.url)]
                item?.let {
                    it.state = PodcastEpisodeItem.STATE_ADDED

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
                    val item = dao.get(PodcastEpisodeItem.convertLinkToId(download.url))
                    item?.let {
                        item.state = PodcastEpisodeItem.STATE_DOWNLOADED
                        dao.update(item)

                        val downloadingItem = downloadingItems[item.id]
                        downloadingItem?.let {
                            downloadingItems.remove(it.id)
                            it.state = PodcastEpisodeItem.STATE_DOWNLOADED
                            it.progress = 100

                            downloadsSubject.onNext(it)
                            val database = getStoredItemsWithProgress() ?: emptyList()
                            databaseChangesSubject.onNext(database)
                            databaseSubject.onNext(database)
                        }
                    }

                    fetch.remove(download.id)

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
                Completable.fromAction {
                    val item = downloadingItems[PodcastEpisodeItem.convertLinkToId(download.url)]
                    item?.let {
                        it.state = PodcastEpisodeItem.STATE_ERROR
                        downloadsSubject.onNext(it)

                        fetch.remove(download.id)
                    }

                    val dao = database.downloadedPodcastItemDao()
                    val dbItem = dao.get(PodcastEpisodeItem.convertLinkToId(download.url))
                    dbItem?.let {
                        dao.delete(dbItem.id)
                    }
                }.subscribeOn(Schedulers.io()).subscribe()
            }

            override fun onPaused(download: Download) {
                internalPause(download, PodcastEpisodeItem.STATE_PAUSED)
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                onItemChanged(download)
                val item = downloadingItems[PodcastEpisodeItem.convertLinkToId(download.url)]
                item?.let {
                    it.state = PodcastEpisodeItem.STATE_DOWNLOADING
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
                internalPause(download, PodcastEpisodeItem.STATE_DOWNLOADING)
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                onItemChanged(download)
            }

            override fun onWaitingNetwork(download: Download) {
                val item = downloadingItems[PodcastEpisodeItem.convertLinkToId(download.url)]
                item?.let {
                    it.state = PodcastEpisodeItem.STATE_WAITING_FOR_NETWORK
                    downloadsSubject.onNext(it)
                }
            }
        })

        /*
        Completable.fromAction {

            val items = getStoredItems()
            items?.filter { it -> it.state != PodcastEpisodeItem.STATE_DOWNLOADED }?.let {
                it.forEach { i ->
                    downloadingItems[i.id] = i

                    val file = getItemLocalPath(i.podcastId ?: "_", i.link!!)
                    val request = Request(i.link!!, file)
                    request.priority = Priority.HIGH
                    request.networkType = NetworkType.ALL
                    fetch.enqueue(request)
                }
            }

            databaseSubject.onNext(items ?: emptyList())
        }.subscribeOn(Schedulers.io()).subscribe()
        */
    }

    private fun internalPause(download: Download, state: Int) {
        Completable.fromAction {
            val dao = database.downloadedPodcastItemDao()
            val id = PodcastEpisodeItem.convertLinkToId(download.url)
            val item = downloadingItems[id]
            item?.let {
                it.state = state
                val dbItem = dao.get(id)
                dbItem?.let {
                    it.state = state
                    dao.update(it)
                }
                downloadsSubject.onNext(it)
            }
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    private fun getStoredItems() = database.downloadedPodcastItemDao()
            .getAll()
            ?.map { it ->
                PodcastDownloadItem(it.id, it.state, 0, it.podcastId, it.podcastTitle,
                        it.title, it.description, it.link, it.pubDate, it.length, it.favourite, it.imageUrl, it.downloadId, it.localPath)
            }

    private fun getStoredItemsWithProgress() = getStoredItems()
            ?.map { it ->
        downloadingItems[it.id]?.let { downloading ->
            it.state = downloading.state
            it.progress = downloading.progress
        }
        it
    }

    private fun onItemChanged(download: Download) {
        fetch.getDownloads(Func { result ->
            val list = mutableListOf<PodcastDownloadItem>()
            result.forEach {
                val item = downloadingItems[PodcastEpisodeItem.convertLinkToId(it.url)]
                item?.let {
                    list.add(item)
                }
            }

            allActiveDownloadsSubject.onNext(list)
        })
    }

    private fun getItemLocalPath(podcastId: String, link: String) = DependencyLocator.getInstance().context.filesDir.absolutePath + "/podcast_items/" + podcastId + "/" + UUID.randomUUID().toString()

    fun subscribeForDownloads(consumer : Consumer<PodcastDownloadItem>) = downloadsSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer)

    fun subscribeForDatabase(consumer: Consumer<List<PodcastDownloadItem>>) = databaseSubject
            .doOnSubscribe {
                databaseSubject.onNext(getStoredItemsWithProgress() ?: emptyList())
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(consumer)

    fun subscribeForDatabaseChanges(consumer: Consumer<List<PodcastDownloadItem>>, observeOn: Scheduler) = databaseChangesSubject
            .subscribeOn(Schedulers.io())
            .observeOn(observeOn).subscribe(consumer)

    fun subscribeForAllActiveDownloads(consumer: Consumer<List<PodcastDownloadItem>>) = allActiveDownloadsSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer)

    fun pause(id: Int) {
        fetch.getDownload(id, Func2 {
            it?.let { download ->
                if (download.status != Status.PAUSED) {
                    fetch.pause(id)
                } else {
                    fetch.resume(id)
                }
            }
        })
    }

    fun download(podcastId: String?, item: Item): Observable<Int> {

        val observable : Observable<Int> = Observable.create { emmiter ->

            item.link?.let {
                val file = getItemLocalPath(podcastId ?: "_", item.link)
                val dao = database.downloadedPodcastItemDao()

                val request = Request(item.link, file)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL

                var storedItem = dao.get(PodcastEpisodeItem.convertLinkToId(item.link))

                if (storedItem == null) {
                    storedItem = buildDatabaseItem(podcastId, item)
                    storedItem.state = PodcastEpisodeItem.STATE_ADDED
                    storedItem.localPath = file
                    storedItem.downloadId = request.id

                    dao.insert(storedItem)
                } else {
                    storedItem.state = PodcastEpisodeItem.STATE_ADDED
                    dao.update(storedItem)
                }


                val downloading = PodcastDownloadItem(storedItem)
                downloadingItems[downloading.id] = downloading

                fetch.enqueue(request)

                val database = getStoredItemsWithProgress() ?: emptyList()
                databaseChangesSubject.onNext(database)
                databaseSubject.onNext(database)
            }
        }

        return observable.subscribeOn(Schedulers.io())
    }

    private fun buildDatabaseItem(podcastId: String?, item: Item) =
        PodcastEpisodeItem(PodcastEpisodeItem.STATE_NONE, podcastId,
                item.podcastTitle, item.title, item.description, item.link, System.currentTimeMillis(),
                item.pubDate, item.length, false, item.imageUrl, 0, null)

    fun favourite(podcastId: String?, item: Item) : Single<List<PodcastDownloadItem>> {
        return Single.fromCallable {
            val dao = database.downloadedPodcastItemDao()

            val id = PodcastEpisodeItem.convertLinkToId(item.link)
            val downloadingItem = downloadingItems[id]
            var storedItem = dao.get(id)
            if (storedItem == null) {
                storedItem = buildDatabaseItem(podcastId, item)
                storedItem.favourite = true
                dao.insert(storedItem)

            } else {
                storedItem.favourite = !storedItem.favourite
                downloadingItem?.favourite = storedItem.favourite
                dao.update(storedItem)
            }

            downloadingItem?.favourite = storedItem.favourite

            getStoredItemsWithProgress() ?: emptyList()
        }.subscribeOn(Schedulers.io())
    }

    fun delete(item: PodcastDownloadItem): Completable {
        return Completable.fromAction {
            fetch.delete(item.downloadId)
            val episode = File(item.localPath)
            if (episode.exists()) {
                episode.delete()
            }
            database.downloadedPodcastItemDao().delete(item.id)

            val database = getStoredItemsWithProgress() ?: emptyList()
            databaseChangesSubject.onNext(database)
            databaseSubject.onNext(database)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun allDownloads() = database.downloadedPodcastItemDao().getAll()
}