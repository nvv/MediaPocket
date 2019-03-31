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
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item
import com.mediapocket.android.model.PlaybackMediaDescriptor

/**
 * @author Vlad Namashko
 */
class PlaybackUnit(val context: Context, val mediaSession: MediaSessionCompat) {

    private var exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            context,
            DefaultRenderersFactory(context),
            DefaultTrackSelector(),
            DefaultLoadControl())

    private val eventListener = EventListener()

    private var mediaConnector: MediaSessionConnector
    private var playbackPreparer: LocalPlaybackPreparer

    private val dataSourceFactory: DefaultDataSourceFactory

    var currentMediaId: String? = null
        private set

    init {
        exoPlayer.addListener(eventListener)

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
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

    fun initWithFeedItems(mediaId: String, items: List<Item>): List<MediaBrowserCompat.MediaItem> {
       return initWithItems(mediaId, items)
    }

    fun initWithLocalEpisodes(mediaId: String, items: List<PodcastEpisodeItem>?): List<MediaBrowserCompat.MediaItem> {
        return initWithItems(mediaId, items)
    }

    private fun initWithItems(mediaId: String, items : List<PlaybackMediaDescriptor>?) : List<MediaBrowserCompat.MediaItem> {
        val metadataList = mutableListOf<MediaMetadataCompat>()
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        items?.forEach {
            val mediaItem = MediaBrowserCompat.MediaItem(it.getMediaDescription(), 0)
            mediaItems.add(mediaItem)
            metadataList.add(it.getMediaMetadataCompat())
        }

        playbackPreparer.playlist = metadataList
        currentMediaId = mediaId

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

        }

        override fun onSeekProcessed() {

        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

        }

        override fun onPlayerError(error: ExoPlaybackException?) {

        }

        override fun onLoadingChanged(isLoading: Boolean) {

        }

        override fun onPositionDiscontinuity(reason: Int) {

        }

        override fun onRepeatModeChanged(repeatMode: Int) {

        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        }

    }

    companion object {
        const val COMMAND_SET_PLAYBACK_RATE = "COMMAND_SET_PLAYBACK_RATE"
        const val COMMAND_RENEW_PLAYLIST = "COMMAND_RENEW_PLAYLIST"

        const val ARG_PLAYBACK_RATE = "ARG_PLAYBACK_RATE"
    }
}