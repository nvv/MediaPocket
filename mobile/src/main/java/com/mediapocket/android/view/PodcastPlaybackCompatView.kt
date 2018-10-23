package com.mediapocket.android.view

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.R
import com.mediapocket.android.extensions.isPlaying
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.TextView
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.SwitchPodcastPlayerModeEvent
import com.mediapocket.android.utils.ViewUtils


/**
 * @author Vlad Namashko
 */
class PodcastPlaybackCompatView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PodcastPlaybackView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private val title: TextView

    init {
        setOnClickListener {
            RxBus.default.postEvent(SwitchPodcastPlayerModeEvent.open())
        }

        title = findViewById(R.id.title)
    }

    override fun getLayout(): Int = R.layout.podcast_playback_compact

    override fun getMediaControllerCallback() = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            visibility = View.VISIBLE

            if (isPlaying != state.isPlaying) {
                isPlaying = state.isPlaying
                playPause.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            title.text = metadata?.description?.title

            metadata?.description?.iconBitmap?.let {
                episodeLogo.setImageBitmap(Bitmap.createScaledBitmap(
                        it, ViewUtils.getDimensionSize(36).toInt(), ViewUtils.getDimensionSize(36).toInt(), false))
            }
        }
    }
}