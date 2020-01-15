package com.mediapocket.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import io.reactivex.Completable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class EpisodesViewModel @Inject constructor(
        private val database: AppDatabase
) : LoadableViewModel() {

    private val _downloadedEpisodes = MutableLiveData<List<PodcastEpisodeItem>>()
    val downloadedEpisodes: LiveData<List<PodcastEpisodeItem>> = _downloadedEpisodes

    private val _favouriteEpisodes = MutableLiveData<List<PodcastEpisodeItem>>()
    val favouriteEpisodes: LiveData<List<PodcastEpisodeItem>> = _favouriteEpisodes

    fun requestDownloadedEpisodes() {
//        return doLoadingAction {
//            Single.fromCallable {
//                database.podcastEpisodeItemDao().getDownloads()
//            }.subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//        }

        GlobalScope.launch {
            _downloadedEpisodes.postValue(database.podcastEpisodeItemDao().getDownloads())
        }
    }

    fun requestFavouritesEpisodes() {
//        return doLoadingAction {
//            Single.fromCallable {
//                database.podcastEpisodeItemDao().getFavourites()
//            }.subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//        }

        GlobalScope.launch {
            _downloadedEpisodes.postValue(database.podcastEpisodeItemDao().getFavourites())
        }
    }

    fun deleteEpisode(item: PodcastDownloadItem): Completable = TODO("delete")

}