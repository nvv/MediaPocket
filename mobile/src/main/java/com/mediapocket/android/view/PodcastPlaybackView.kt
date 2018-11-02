package com.mediapocket.android.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.VolumeProviderCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.R
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.utils.ViewUtils

/**
 * @author Vlad Namashko
 */
abstract class PodcastPlaybackView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    protected var mediaConnection: MediaSessionConnection? = null

    protected var isPlaying: Boolean = false

    private val mediaControllerCallback = getMediaControllerCallback()

    protected val episodeLogo: ImageView
    protected val playPause: ImageView
    protected val fastForward: ImageView

    init {
        LayoutInflater.from(context).inflate(getLayout(), this)
        episodeLogo = findViewById(R.id.episode_logo)
        playPause = findViewById(R.id.play_pause)

        playPause.setOnClickListener {

            val controls = mediaConnection?.mediaController?.transportControls
            mediaConnection?.mediaController?.playbackState?.let {
                isPlaying = !isPlaying
                val icon = resources.getDrawable(if (it.isPlaying) R.drawable.ic_pause_animated else R.drawable.ic_play_animated, null) as AnimatedVectorDrawable
                playPause.setImageDrawable(icon)
                icon.start()

                if (it.isPlaying) {
                    controls?.pause()
                } else {
                    controls?.play()
                }
            }
        }

        fastForward = findViewById(R.id.fastforward)

        fastForward.setOnClickListener {
            rotate(fastForward, 0f, 90f)
            mediaConnection?.mediaController?.transportControls?.fastForward()
        }
    }

    open fun initWithMediaConnection(mediaConnection: MediaSessionConnection) {
        mediaConnection.registerMediaControllerCallback(mediaControllerCallback)
        this.mediaConnection = mediaConnection
    }

    fun detachMediaConnectionCallback() {
        mediaConnection?.unregisterMediaControllerCallback(mediaControllerCallback)
    }

    protected fun rotate(view: View, from: Float, to: Float) {
        var rotate = RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.55f)
        rotate.duration = 250
        rotate.interpolator = LinearInterpolator()

        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                rotate = RotateAnimation(to, from, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.55f)
                rotate.duration = 250
                rotate.interpolator = LinearInterpolator()
                view.startAnimation(rotate)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        view.startAnimation(rotate)
    }

    abstract fun getLayout(): Int

    abstract fun getMediaControllerCallback(): MediaControllerCompat.Callback
}