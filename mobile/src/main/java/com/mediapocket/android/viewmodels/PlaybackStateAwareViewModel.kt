package com.mediapocket.android.viewmodels

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.core.download.model.DownloadError
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.details.viewitem.DownloadState
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.repository.PodcastEpisodeRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

abstract class PlaybackStateAwareViewModel(
        private val downloadManager: PodcastDownloadManager,
        private val podcastEpisodeRepository: PodcastEpisodeRepository,
        private val errorMapper: DownloadErrorToStringMapper,
        private val viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper
) : LoadableViewModel() {

    private val mappedItems = mutableMapOf<PodcastEpisodeViewItem, PodcastEpisodeItem>()

    // TODO: tmp
    var episodeItems: List<PodcastEpisodeViewItem>? = null

    protected lateinit var mediaConnection: MediaSessionConnection
    private lateinit var mediaCallback: MediaControllerCompat.Callback

    private val _episodesChanged = MutableLiveData<Set<Int>>()
    val episodesChanged: LiveData<Set<Int>> = _episodesChanged

    fun initMediaCallback(context: Context) {
        mediaCallback = object : MediaControllerCompat.Callback() {
            private var playingEpisodeId: String? = null

            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                mediaConnection.mediaController.metadata?.description?.mediaId?.let { it ->
                    itemPlaybackChanged(it, state.isPlaying)
                }

            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                metadata?.description?.mediaId?.let {
                    itemPlaybackChanged(it, mediaConnection.mediaController.playbackState.isPlaying)
                }
            }

            private fun itemPlaybackChanged(itemLink: String, playing: Boolean) {
                val id = PodcastEpisodeItem.convertLinkToId(itemLink)

                val changed = mutableSetOf<Int>()
                if (playingEpisodeId != id) {
                    playingEpisodeId?.let {
                        episodeItems?.find { item -> item.id == it }?.apply {
                            isPlaying = false
                            changed.add(position)
                        }
                    }
                }

                episodeItems?.find { item -> item.id == id }?.apply {
                    if (isPlaying != playing) {
                        changed.add(position)
                    }
                    isPlaying = playing
                    playingEpisodeId = id
                }

                if (changed.isNotEmpty()) {
                    notifyEpisodesIndexesChanged(changed)
                }
            }
        }

        mediaConnection = MediaSessionConnection.getInstance(context).apply {
            registerMediaControllerCallback(mediaCallback)
        }
    }

    protected fun handleDownloadProgress(episode: PodcastEpisodeViewItem, item: PodcastDownloadItem) {
        if (episode.downloadState == null) {
            episode.downloadState = DownloadState()
        }

        episode.downloadState?.state = item.state
        episode.downloadState?.progress = item.progress
        // TODO
        episode.downloadState?.isDownloaded = item.progress == 100
        item.error?.let {
            episode.downloadState?.error = mapError(it)
        }
        notifyEpisodesIndexesChanged(setOf(episode.position))
    }

    protected fun listenForActiveDownloads(downloadManager: PodcastDownloadManager, podcastId: String? = null) {
        downloadManager.getActiveDownloads(podcastId)?.forEach { item ->
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

    override fun onCleared() {
        super.onCleared()
        mediaConnection.unregisterMediaControllerCallback(mediaCallback)
    }

    fun favouriteEpisode(episode: PodcastEpisodeViewItem) {
        GlobalScope.launch {
            episode.isFavourite = podcastEpisodeRepository.toggleFavourite(mapToEpisodeDbItem(episode))
            _episodesChanged.postValue(setOf(episode.position))
        }
    }

    fun downloadItem(episode: PodcastEpisodeViewItem) {
        val process = downloadManager.download(mapToEpisodeDbItem(episode))

        GlobalScope.launch {
            process?.consumeEach { item ->
                handleDownloadProgress(episode, item)
            }
        }
    }

    private fun mapToEpisodeDbItem(episode: PodcastEpisodeViewItem): PodcastEpisodeItem {
        return mappedItems[episode]?.let {
            it
        } ?: run {
            val item = viewItemToDatabaseItemMapper.map(episode)
            mappedItems[episode] = item
            item
        }
    }

    fun pauseDownload(episode: PodcastEpisodeViewItem) {
        downloadManager.pauseDownload(episode.id)
    }

    fun resumeDownload(episode: PodcastEpisodeViewItem) {
        downloadManager.resumeDownload(episode.id)
    }

    private fun mapError(error: DownloadError): String? = errorMapper.map(error)

    protected fun notifyEpisodesIndexesChanged(episodes: Set<Int>) = _episodesChanged.postValue(episodes)

}