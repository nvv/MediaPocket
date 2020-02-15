package com.mediapocket.android.episodes.vm

import android.content.Context
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.episodes.viewitem.EpisodeDatabaseItemToViewItemMapper
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.viewmodels.PlaybackStateAwareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * @author Vlad Namashko
 */
abstract class EpisodesViewModel constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        private val podcastEpisodeRepository: PodcastEpisodeRepository,
        private val downloadManager: PodcastDownloadManager
) : PlaybackStateAwareViewModel(downloadManager, podcastEpisodeRepository, errorMapper, viewItemToDatabaseItemMapper) {

    var coroutineJob: Job? = null

    init {
        initMediaCallback(context)
    }

    fun initModel() {
        coroutineJob = GlobalScope.launch {
            reloadEpisodes()

            listenForActiveDownloads(downloadManager)
            listenForEpisodesChanged()
        }
    }

    private suspend fun listenForEpisodesChanged() {
        podcastEpisodeRepository.episodes.consumeEach {
            reloadEpisodes()
        }
    }

    override fun onEpisodeDeleted(item: PodcastEpisodeViewItem) {
    }

    override fun onCleared() {
        super.onCleared()
        coroutineJob?.cancel()
    }

    abstract fun reloadEpisodes()
}