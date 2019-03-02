package com.mediapocket.android.view

import android.content.Context
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.widget.SeekBar
import android.animation.ValueAnimator
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.animation.LinearInterpolator
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.mediapocket.android.R
import kotlinx.android.synthetic.main.podcast_playback_expanded.view.*


/**
 * @author Vlad Namashko
 */
class MediaSeekBarView : FrameLayout {
    private var mediaController: MediaControllerCompat? = null
    private var controllerCallback: ControllerCallback? = null

    private var isTracking = false

    private var seekBar: SeekBar
    private var timePlayed: TextView
    private var timeLeft: TextView

    private val mOnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            isTracking = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            mediaController?.transportControls?.seekTo(seekBar.progress.toLong())
            isTracking = false
        }
    }
    private var mProgressAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.media_seekbar_view, this)
        seekBar = findViewById(R.id.playback_seek_bar)
        timeLeft = findViewById(R.id.time_left)
        timePlayed = findViewById(R.id.time_played)

        seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
    }

    fun setMediaController(mediaController: MediaControllerCompat?) {
        if (mediaController != null) {
            controllerCallback = ControllerCallback()
            mediaController.registerCallback(controllerCallback!!)
        } else if (this.mediaController != null) {
            this.mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
        }
        this.mediaController = mediaController
    }

    fun disconnectController() {
        if (mediaController != null) {
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
            mediaController = null
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), ValueAnimator.AnimatorUpdateListener {

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator!!.cancel()
                mProgressAnimator = null
            }

            val progress = state?.position?.toInt() ?: 0
            seekBar.progress = progress

            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).
            if (state != null && state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((seekBar.max - progress) / state.playbackSpeed).toInt()

                mProgressAnimator = ValueAnimator.ofInt(progress, seekBar.max)
                        .setDuration(if (timeToEnd > 0) timeToEnd.toLong() else 1)
                mProgressAnimator?.interpolator = LinearInterpolator()
                mProgressAnimator?.addUpdateListener(this)
                mProgressAnimator?.start()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0
            seekBar.progress = 0
            seekBar.max = max
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            // If the user is changing the slider, cancel the animation.
            if (isTracking) {
                valueAnimator.cancel()
                return
            }

            val animatedIntValue = valueAnimator.animatedValue as Int
            seekBar.progress = animatedIntValue

            timePlayed.text = timeFormatted(seekBar.progress)
            timeLeft.text = timeFormatted(seekBar.max - seekBar.progress)
        }

        private fun timeFormatted(millis: Int): String {
            //val millis = millis % 1000
            val second = millis / 1000 % 60
            val minute = millis / (1000 * 60) % 60
            val hour = millis / (1000 * 60 * 60) % 24

            return if (hour > 0) String.format(formatLong, hour, minute, second) else String.format(formatShort, minute, second)
        }
    }

    companion object {
        const val formatLong = "%02d:%02d:%02d"
        const val formatShort = "%02d:%02d"
    }

}