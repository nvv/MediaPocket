package com.mediapocket.android.episodes.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.episodes.viewitem.EpisodeDatabaseItemToViewItem
import com.mediapocket.android.repository.PodcastEpisodeRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadedEpisodesViewModel @Inject constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        podcastEpisodeRepository: PodcastEpisodeRepository,
        private val repository: PodcastEpisodeRepository,
        private val mapper: EpisodeDatabaseItemToViewItem,
        private val downloadManager: PodcastDownloadManager
) : EpisodesViewModel(context, errorMapper, viewItemToDatabaseItemMapper, podcastEpisodeRepository, downloadManager) {


    private val _downloadedEpisodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val downloadedEpisodes: LiveData<List<PodcastEpisodeViewItem>> = _downloadedEpisodes

    fun requestDownloadedEpisodes() {
        GlobalScope.launch {
            episodeItems = repository.getDownloads()?.mapIndexed { index, item ->
                mapper.map(
                        index,
                        item,
                        mediaConnection.playbackState?.isPlaying == true,
                        mediaConnection.playbackMetadata)
            }
            _downloadedEpisodes.postValue(episodeItems)
            listenForActiveDownloads(downloadManager)
        }
    }

    fun deleteEpisode(item: PodcastEpisodeViewItem) {
        GlobalScope.launch {
            repository.deleteEpisode(item.id)
            _downloadedEpisodes.postValue(repository.getDownloads()?.mapIndexed { index, item ->
                mapper.map(
                        index,
                        item,
                        mediaConnection.playbackState?.isPlaying == true,
                        mediaConnection.playbackMetadata)
            })
        }
    }


}