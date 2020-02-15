package com.mediapocket.android.episodes.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.episodes.viewitem.EpisodeDatabaseItemToViewItemMapper
import com.mediapocket.android.repository.PodcastEpisodeRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadedEpisodesViewModel @Inject constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        private val repository: PodcastEpisodeRepository,
        private val mapper: EpisodeDatabaseItemToViewItemMapper,
        private val downloadManager: PodcastDownloadManager
) : EpisodesViewModel(context, errorMapper, viewItemToDatabaseItemMapper, repository, downloadManager) {

    private val _downloadedEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val downloadedEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _downloadedEpisodes

    fun requestDownloadedEpisodes() {
        initModel()
    }

    override fun reloadEpisodes() {
        episodeItems = repository.getDownloads()?.mapIndexed { index, item ->
            mapper.map(
                    index,
                    item,
                    mediaConnection.playbackState?.isPlaying == true,
                    mediaConnection.playbackMetadata)
        }
        _downloadedEpisodes.postValue(episodeItems)
    }

}