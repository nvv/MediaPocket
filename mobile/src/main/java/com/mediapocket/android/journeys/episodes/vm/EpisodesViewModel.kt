package com.mediapocket.android.journeys.episodes.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.viewmodels.LoadableViewModel
import io.reactivex.Completable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class EpisodesViewModel @Inject constructor(
        private val repository: PodcastEpisodeRepository
) : LoadableViewModel() {

    private val _downloadedEpisodes = MutableLiveData<List<PodcastEpisodeItem>>()
    val downloadedEpisodes: LiveData<List<PodcastEpisodeItem>> = _downloadedEpisodes

    private val _favouriteEpisodes = MutableLiveData<List<PodcastEpisodeItem>>()
    val favouriteEpisodes: LiveData<List<PodcastEpisodeItem>> = _favouriteEpisodes

    fun requestDownloadedEpisodes() {
        GlobalScope.launch {
            _downloadedEpisodes.postValue(repository.getDownloads())
        }
    }

    fun requestFavouritesEpisodes() {
        GlobalScope.launch {
            _favouriteEpisodes.postValue(repository.getFavourites())
        }
    }

    fun deleteEpisode(item: PodcastEpisodeItem) {
        GlobalScope.launch {
            repository.deleteEpisode(item)
            _downloadedEpisodes.postValue(repository.getDownloads())
        }
    }

}