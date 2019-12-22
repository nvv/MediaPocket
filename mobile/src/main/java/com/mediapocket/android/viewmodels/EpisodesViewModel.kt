package com.mediapocket.android.viewmodels

import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class EpisodesViewModel @Inject constructor(
        private val database: AppDatabase,
        private val manager: PodcastDownloadManager
) : LoadableViewModel() {

    fun getDownloadedEpisodes(): Single<List<PodcastEpisodeItem>?> {
        return doLoadingAction {
            Single.fromCallable {
                database.downloadedPodcastItemDao().getDownloaded()
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun getFavouritesEpisodes(): Single<List<PodcastEpisodeItem>?> {
        return doLoadingAction {
            Single.fromCallable {
                database.downloadedPodcastItemDao().getFavourites()
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun deleteEpisode(item: PodcastDownloadItem): Completable = manager.delete(item)

}