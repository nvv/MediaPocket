package com.mediapocket.android.playback

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.StopPlaybackEvent
import com.mediapocket.android.extensions.id
import com.mediapocket.android.extensions.mediaUriString
import com.mediapocket.android.extensions.toMediaSource
import com.mediapocket.android.playback.PlaybackUnit.Companion.ARG_PLAYBACK_RATE
import com.mediapocket.android.playback.PlaybackUnit.Companion.COMMAND_RENEW_PLAYLIST
import com.mediapocket.android.playback.PlaybackUnit.Companion.COMMAND_SET_PLAYBACK_RATE
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author Vlad Namashko
 */
class LocalPlaybackPreparer(
        private val context: Context,
        private val dataSourceFactory: DataSource.Factory,
        private val session: MediaSessionCompat,
        private val exoPlayer: ExoPlayer) : MediaSessionConnector.PlaybackPreparer {

    lateinit var playlist: MutableList<MediaMetadataCompat>

    private lateinit var currentPlaylist: MutableList<MediaMetadataCompat>
    private var concatenatingMediaSource = ConcatenatingMediaSource()

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {

    }

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) {
        if (command == COMMAND_SET_PLAYBACK_RATE) {
            extras?.let {
                exoPlayer.playbackParameters = PlaybackParameters(it.getFloat(ARG_PLAYBACK_RATE))
            }
        } else if (command == COMMAND_RENEW_PLAYLIST) {

            var currentlyPlayedItemRemoved = false

            // remove tracks from old playlist which doesn't present in current
            ConcurrentLinkedQueue(currentPlaylist).forEachIndexed { index, item ->
                val track = playlist.find { it.id == item.id }
                track ?: run {

                    // currently played item is removed - stop playing
                    if (exoPlayer.currentWindowIndex == index) {
                        exoPlayer.stop()
                        RxBus.default.postEvent(StopPlaybackEvent())
                        return
                    }

                    currentPlaylist.removeAt(index)
                    concatenatingMediaSource.removeMediaSource(index)
                }
            }

            playlist.forEachIndexed { index, item ->
                currentPlaylist.find { it.id == item.id }?.apply {
                    val position = currentPlaylist.indexOf(this)

                    // move tracks on correct position
                    if (position != index) {
                        concatenatingMediaSource.removeMediaSource(position)
                        concatenatingMediaSource.addMediaSource(index, item.toMediaSource(dataSourceFactory))
                    }
                } ?: run {
                    // add new track on desired position
                    concatenatingMediaSource.addMediaSource(index, item.toMediaSource(dataSourceFactory))
                }
            }

            currentPlaylist = playlist

            if (currentlyPlayedItemRemoved && !currentPlaylist.isEmpty()) {
                exoPlayer.seekTo(0, 0)
            }

        }
    }

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH


    override fun getCommands(): Array<String>? = arrayOf(COMMAND_SET_PLAYBACK_RATE, COMMAND_RENEW_PLAYLIST)

    override fun onPrepareFromMediaId(mediaId: String?, bundle: Bundle) {

        val data = playlist.find { item -> item.mediaUriString == mediaId }

        if (data == null) {
            exoPlayer.seekTo(0, 0)
        } else {
            concatenatingMediaSource.clear()

            currentPlaylist = playlist
            playlist.forEach { item ->
                concatenatingMediaSource.addMediaSource(item.toMediaSource(dataSourceFactory))
            }
            exoPlayer.prepare(concatenatingMediaSource)
            exoPlayer.playWhenReady = true

            val initialWindowIndex = playlist.indexOf(data)
            exoPlayer.seekTo(initialWindowIndex, 0)
        }
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {

    }

    override fun onPrepare() = Unit

}