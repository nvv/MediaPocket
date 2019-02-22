package com.mediapocket.android.viewmodels

import android.arch.lifecycle.ViewModel
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.di.MainComponentLocator
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesViewModel : LoadableViewModel() {

    @set:Inject
    lateinit var database: AppDatabase

    @set:Inject
    lateinit var manager: PodcastDownloadManager

    init {
        MainComponentLocator.mainComponent.inject(this)
    }

    fun getDownloadedEpisodes(): Single<List<PodcastEpisodeItem>?> {
        return doLoadingAction {
            Single.fromCallable {
                database.downloadedPodcastItemDao().getAll()
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun deleteEpisode(item: PodcastDownloadItem): Completable = manager.delete(item)

}