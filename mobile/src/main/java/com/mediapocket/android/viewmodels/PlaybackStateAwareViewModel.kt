package com.mediapocket.android.viewmodels

import android.app.DownloadManager
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
import com.mediapocket.android.journeys.details.viewitem.DownloadState
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

abstract class PlaybackStateAwareViewModel : LoadableViewModel() {

    // TODO: tmp
    protected var episodeItems : List<PodcastEpisodeViewItem>? = null

    protected lateinit var mediaConnection: MediaSessionConnection
    private lateinit var mediaCallback: MediaControllerCompat.Callback

    protected val _episodesChanged = MutableLiveData<Set<Int>>()
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

    protected fun notifyEpisodesIndexesChanged(episodes: Set<Int>) = _episodesChanged.postValue(episodes)

    protected abstract fun mapError(error: DownloadError): String?
}