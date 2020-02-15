package com.mediapocket.android.episodes.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.episodes.viewitem.EpisodeDatabaseItemToViewItemMapper
import com.mediapocket.android.repository.PodcastEpisodeRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class FavouritesEpisodesViewModel @Inject constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        podcastEpisodeRepository: PodcastEpisodeRepository,
        private val repository: PodcastEpisodeRepository,
        private val mapper: EpisodeDatabaseItemToViewItemMapper,
        private val downloadManager: PodcastDownloadManager
) : EpisodesViewModel(context, errorMapper, viewItemToDatabaseItemMapper, podcastEpisodeRepository, downloadManager) {

    private val _favouriteEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val favouriteEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _favouriteEpisodes

    fun requestFavouritesEpisodes() {
        initModel()
    }

    override fun reloadEpisodes() {
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
