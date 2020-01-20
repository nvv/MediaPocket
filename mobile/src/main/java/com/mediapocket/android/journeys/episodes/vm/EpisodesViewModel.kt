package com.mediapocket.android.journeys.episodes.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.core.download.model.DownloadError
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.journeys.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.journeys.episodes.viewitem.EpisodeDatabaseItemToViewItem
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.viewmodels.LoadableViewModel
import com.mediapocket.android.viewmodels.PlaybackStateAwareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class EpisodesViewModel @Inject constructor(
        private val context: Context,
        private val repository: PodcastEpisodeRepository,
        private val mapper: EpisodeDatabaseItemToViewItem,
        private val errorMapper: DownloadErrorToStringMapper,
        private val downloadManager: PodcastDownloadManager
) : PlaybackStateAwareViewModel() {

    private val _downloadedEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val downloadedEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _downloadedEpisodes

    private val _favouriteEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val favouriteEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _favouriteEpisodes

    init {
        initMediaCallback(context)
    }

    fun requestDownloadedEpisodes() {
        GlobalScope.launch {
            episodeItems = repository.getDownloads()?.mapIndexed { index, item -> mapper.map(index, item) }
            _downloadedEpisodes.postValue(episodeItems)
            listenForactiveDownloads()
        }
    }

    fun requestFavouritesEpisodes() {
        GlobalScope.launch {
            episodeItems = repository.getFavourites()?.mapIndexed { index, item -> mapper.map(index, item) }
            _favouriteEpisodes.postValue(episodeItems)
            listenForactiveDownloads()
        }
    }

    fun deleteEpisode(item: PodcastEpisodeViewItem) {
        GlobalScope.launch {
            repository.deleteEpisode(item.id)
            _downloadedEpisodes.postValue(repository.getDownloads()?.mapIndexed { index, item -> mapper.map(index, item) })
        }
    }

    private fun listenForactiveDownloads() {
        downloadManager.getActiveDownloads()?.forEach { item ->
            episodeItems?.find { it -> it.id == item.id }?.let { episode ->
                downloadManager.listenForDownloadProgress(episode.id)?.let { process ->
                    GlobalScope.launch {
                        process?.consumeEach { item ->
                            handleDownloadProgress(episode, item)
                        }
                    }
                }
            }
        }
    }

    override fun mapError(error: DownloadError): String? = errorMapper.map(error)
}