package com.mediapocket.android.playback

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
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
import com.google.android.exoplayer2.upstream.DataSource
import com.mediapocket.android.extensions.mediaUriString
import com.mediapocket.android.extensions.toMediaSource
import com.mediapocket.android.playback.LocalPlayback.Companion.ARG_PLAYBACK_RATE
import com.mediapocket.android.playback.LocalPlayback.Companion.COMMAND_SET_PLAYBACK_RATE

/**
 * @author Vlad Namashko
 */
class LocalPlaybackPreparer(
        private val context: Context,
        private val dataSourceFactory: DataSource.Factory,
        private val session: MediaSessionCompat,
        private val exoPlayer: ExoPlayer) : MediaSessionConnector.PlaybackPreparer {

    lateinit var playlist: MutableList<MediaMetadataCompat>

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {

    }

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) {
        if (command == COMMAND_SET_PLAYBACK_RATE) {
            extras?.let {
                exoPlayer.playbackParameters = PlaybackParameters(it.getFloat(ARG_PLAYBACK_RATE))
            }
        }
    }

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH


    override fun getCommands(): Array<String>? = arrayOf(COMMAND_SET_PLAYBACK_RATE)

    override fun onPrepareFromMediaId(mediaId: String?, bundle: Bundle) {

        val data = playlist.find { item -> item.mediaUriString == mediaId }

        val mediaSource = playlist.toMediaSource(dataSourceFactory)
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true

        if (data == null) {
            exoPlayer.seekTo(0, 0)
        } else {
            val initialWindowIndex = playlist.indexOf(data)
            exoPlayer.seekTo(initialWindowIndex, 0)
        }
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {

    }

    override fun onPrepare() = Unit

}