package com.mediapocket.android.journeys.episodes.vm

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.NotificationBuilder
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.journeys.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.journeys.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.journeys.episodes.viewitem.EpisodeDatabaseItemToViewItem
import com.mediapocket.android.repository.PodcastEpisodeRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class FavouritesEpisodesViewModel @Inject constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        podcastEpisodeRepository: PodcastEpisodeRepository,
        private val repository: PodcastEpisodeRepository,
        private val mapper: EpisodeDatabaseItemToViewItem,
        private val downloadManager: PodcastDownloadManager
) : EpisodesViewModel(context, errorMapper, viewItemToDatabaseItemMapper, podcastEpisodeRepository, downloadManager) {

    private val _favouriteEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val favouriteEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _favouriteEpisodes

    fun requestFavouritesEpisodes() {
        GlobalScope.launch {
            episodeItems = repository.getFavourites()?.mapIndexed { index, item ->
                mapper.map(
                        index,
                        item,
                        mediaConnection.playbackState?.isPlaying == true,
                        mediaConnection.playbackMetadata)
            }
            _favouriteEpisodes.postValue(episodeItems)
            listenForActiveDownloads(downloadManager)
            listenForFavouritesChanged()
        }
    }

    private suspend fun listenForFavouritesChanged() {
        repository.favourites.consumeEach {
            episodeItems = repository.getFavourites()?.mapIndexed { index, item ->
                mapper.map(
                        index,
                        item,
                        mediaConnection.playbackState?.isPlaying == true,
                        mediaConnection.playbackMetadata)
            }
            _favouriteEpisodes.postValue(episodeItems)
        }
    }

}
