package com.mediapocket.android.playback

import android.content.Context
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.mediapocket.android.model.Item

/**
 * @author Vlad Namashko
 */
class LocalPlayback(val context: Context, val mediaSession: MediaSessionCompat) {

    var exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(context),
            DefaultTrackSelector(),
            DefaultLoadControl())

    private val eventListener = EventListener()

//    private var extractorMediaFactory: ExtractorMediaSource.Factory

    private var mediaConnector: MediaSessionConnector
    private var playbackPreparer: LocalPlaybackPreparer

    private val dataSourceFactory: DefaultDataSourceFactory

    init {
        exoPlayer.addListener(eventListener)

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()
        exoPlayer.audioAttributes = audioAttributes

        val userAgent = Util.getUserAgent(context, "MediaPocket")

        // Default parameters, except allowCrossProtocolRedirects is true
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                userAgent,
                null /* listener */,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */
        )

        dataSourceFactory = DefaultDataSourceFactory(context, DefaultBandwidthMeter(), httpDataSourceFactory)

        // Produces Extractor instances for parsing the media data.
        val extractorsFactory = DefaultExtractorsFactory()
        // The MediaSource represents the media to be played.
        val extractorMediaFactory = ExtractorMediaSource.Factory(dataSourceFactory)
        extractorMediaFactory.setExtractorsFactory(extractorsFactory)

        playbackPreparer = LocalPlaybackPreparer(context, dataSourceFactory, mediaSession, exoPlayer)
        mediaConnector = MediaSessionConnector(mediaSession, DefaultPlaybackController(10000, 30000, MediaSessionConnector.DEFAULT_REPEAT_TOGGLE_MODES)).also {
            it.setPlayer(exoPlayer, playbackPreparer)
        }




        mediaConnector.setQueueNavigator(QueueNavigator(mediaSession))
    }

    fun initWithFeedItems(mediaId: String, items: MutableList<Item>): List<MediaBrowserCompat.MediaItem> {
        val metadataList = mutableListOf<MediaMetadataCompat>()
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        items.forEach {
            val mediaItem = MediaBrowserCompat.MediaItem(it.getMediaDescription(), 0)
            mediaItems.add(mediaItem)
            metadataList.add(it.getMediaMetadataCompat())
        }

//        val mediaSource = metadataList.toMediaSource(dataSourceFactory)
//        exoPlayer.prepare(mediaSource)
        playbackPreparer.playlist = metadataList

        return mediaItems
    }

    private inner class QueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession) {

        private val window = Timeline.Window()
        private var isLoading = false

        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return player.currentTimeline.getWindow(windowIndex, window, true).tag as MediaDescriptionCompat
        }

    }


    private class EventListener : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            println("onPlaybackParametersChanged")
        }

        override fun onSeekProcessed() {
            println("onSeekProcessed")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            println("onTracksChanged")
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            println("onPlayerError")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            println("onLoadingChanged")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            println("onPositionDiscontinuity")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            println("onRepeatModeChanged")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            println("onShuffleModeEnabledChanged")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            println("onTimelineChanged")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            println("onPlayerStateChanged")
        }

    }

    companion object {
        const val COMMAND_SET_PLAYBACK_RATE = "COMMAND_SET_PLAYBACK_RATE"
        const val ARG_PLAYBACK_RATE = "ARG_PLAYBACK_RATE"
    }
}