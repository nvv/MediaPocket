package com.mediapocket.android.view

import android.animation.*
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.ChangeStatusBarColorEvent
import com.mediapocket.android.events.SwitchPodcastPlayerModeEvent
import com.mediapocket.android.events.VolumeLevelKeyEvent
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.playback.LocalPlayback.Companion.ARG_PLAYBACK_RATE
import com.mediapocket.android.playback.LocalPlayback.Companion.COMMAND_SET_PLAYBACK_RATE
import com.mediapocket.android.utils.ViewUtils
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.podcast_playback_expanded.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundDrawable


/**
 * @author Vlad Namashko
 */
class PodcastPlaybackExpandedView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PodcastPlaybackView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private var disposable: CompositeDisposable? = null

    private val gestureDetector: GestureDetectorCompat

    private var currentBgPrimaryColor: Int = -1

    init {
        close.setOnClickListener { RxBus.default.postEvent(SwitchPodcastPlayerModeEvent.close()) }

        next.setOnClickListener {
            mediaConnection?.mediaController?.playbackState?.let { state ->
                if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L) {
                    (next.drawable as Animatable).start()
                    mediaConnection?.mediaController?.transportControls?.skipToNext()
                }
            }
        }

        prev.setOnClickListener {
            mediaConnection?.mediaController?.playbackState?.let { state ->
                if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L) {
                    (prev.drawable as Animatable).start()
                    mediaConnection?.mediaController?.transportControls?.skipToPrevious()
                }
            }
        }

        rewind.setOnClickListener{
            rotate(rewind, 0f, -90f)
            mediaConnection?.mediaController?.transportControls?.rewind()
        }

        gestureDetector = GestureDetectorCompat(context, object : GestureListener(this@PodcastPlaybackExpandedView) {
            override fun onSwipeBottom(): Boolean {
                RxBus.default.postEvent(SwitchPodcastPlayerModeEvent.close())
                return super.onSwipeBottom()
            }

            override fun onSwipeRight(): Boolean {
                mediaConnection?.mediaController?.transportControls?.skipToNext()
                return super.onSwipeRight()
            }

            override fun onSwipeLeft(): Boolean {
                mediaConnection?.mediaController?.transportControls?.skipToPrevious()
                return super.onSwipeRight()
            }

        })
    }

    fun setDisposable(disposable: CompositeDisposable) {
        this.disposable = disposable
        disposable.add(RxBus.default.observerFor(VolumeLevelKeyEvent::class.java).subscribe {
            if (volume_level.progress != it.volume) {
                volume_level.progress = it.volume
            }
        })
    }

    override fun getLayout(): Int = R.layout.podcast_playback_expanded

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun initWithMediaConnection(mediaConnection: MediaSessionConnection) {
        super.initWithMediaConnection(mediaConnection)
        media_seek_bar_view.setMediaController(mediaConnection.mediaController)

        mediaConnection.mediaController.playbackInfo?.let {
            val maxVolume = it.maxVolume
            volume_level.max = maxVolume
            volume_level.progress = it.currentVolume
            volume_level.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaConnection.mediaController.setVolumeTo(progress, 0)
                    }
                }
            })

            volume_min.setOnClickListener {
                mediaConnection.mediaController.setVolumeTo(0, 0)
            }

            volume_max.setOnClickListener {
                mediaConnection.mediaController.setVolumeTo(maxVolume, 0)
            }

            playback_speed.tag = 10
            playback_speed.setOnClickListener {
                val popDialog = AlertDialog.Builder(context)
                val seek = SeekBar(context)
                seek.setPadding(seek.paddingLeft, ViewUtils.getDimensionSize(16).toInt(), seek.paddingRight, seek.paddingBottom)
                seek.max = 40
                seek.progress = playback_speed.tag as Int

                popDialog.setTitle(R.string.select_playback_speed)
                popDialog.setView(seek)

                seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        var rate = calculateRate(progress)

                        val args = Bundle()
                        args.putFloat(ARG_PLAYBACK_RATE, rate)
                        mediaConnection.mediaController.sendCommand(COMMAND_SET_PLAYBACK_RATE, args, null)
                    }

                    override fun onStartTrackingTouch(arg0: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                    }
                })

                popDialog.setPositiveButton(R.string.btn_ok) { dialog, which ->
                    playback_speed.text = "%sx".format(calculateRate(seek.progress))
                    playback_speed.tag = seek.progress
                    dialog.dismiss()
                }
                popDialog.setNegativeButton(R.string.btn_cancel) { dialog, which ->
                    val speed = calculateRate(playback_speed.tag as Int)
                    playback_speed.text = "%sx".format(speed)

                    val args = Bundle()
                    args.putFloat(ARG_PLAYBACK_RATE, speed)
                    mediaConnection.mediaController.sendCommand(COMMAND_SET_PLAYBACK_RATE, args, null)
                    dialog.dismiss()
                }


                popDialog.create()
                popDialog.show()
            }
        }
    }

    private fun calculateRate(progress: Int): Float {
        return when {
            progress < 10 -> 1 - (10 - progress) / 20f
            progress > 10 -> 1 + (progress - 10) / 20f
            else -> 1.0f
        }
    }

    override fun getMediaControllerCallback() = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {

            if (isPlaying != state.isPlaying && state.state != PlaybackStateCompat.STATE_BUFFERING) {
                isPlaying = state.isPlaying

                val stateSet = if (isPlaying) intArrayOf(-R.attr.state_play, R.attr.state_pause) else intArrayOf(R.attr.state_play, -R.attr.state_pause)
                playPause.setImageState(stateSet, true)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.description?.let {
                // change episode logo and background
                it.iconBitmap?.let {

                    if (episodeLogo.tag == null || !it.sameAs(episodeLogo.tag as Bitmap)) {
                        val alpha = ObjectAnimator.ofFloat(episodeLogo, "alpha", 1f, 0f).setDuration(IMAGE_FADE_IN_OUT_DURATION)
                        alpha.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                episodeLogo.setImageBitmap(it)
                                episodeLogo.tag = it
                                ObjectAnimator.ofFloat(episodeLogo, "alpha", 0f, 1f).setDuration(IMAGE_FADE_IN_OUT_DURATION).start()
                            }
                        })
                        alpha.start()
                    }

                    Palette.from(it).generate { palette ->

                        if (palette != null) {
                            val color = palette.getDarkVibrantColor(R.attr.colorPrimary)
                            val color2 = color or 0xFF000000.toInt()
                            val color1 = manipulateColor(color2, 0.6f)

                            if (color2 != currentBgPrimaryColor) {
                                if (currentBgPrimaryColor == -1) {
                                    setGradientBackground(color2, color1)
                                } else {
                                    val animation = ValueAnimator.ofObject(ArgbEvaluator(), currentBgPrimaryColor, color2)
                                    animation.duration = COLOR_TRANSITION_DURATION
                                    animation.addUpdateListener { animator ->
                                        val curColor = animator.animatedValue as Int
                                        setGradientBackground(curColor, manipulateColor(curColor, 0.6f))
                                    }
                                    animation.start()
                                }

                                // TODO
                                close.colorFilter = PorterDuffColorFilter(palette.getLightVibrantColor(
                                        resources.getColor(R.color.white)), PorterDuff.Mode.SRC_ATOP)

                            }
                        }
                    }
                }

                title.text = it.title
                subtitle.text = it.subtitle

                it.extras?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            }
        }

        private fun setGradientBackground(color2: Int, color1: Int) {
            currentBgPrimaryColor = color2
            val gradient = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(color2, color1))
            gradient.cornerRadius = 0f
            RxBus.default.postEvent(ChangeStatusBarColorEvent(color2))
            root_view.backgroundDrawable = gradient
        }

    }

    fun manipulateColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.round(Color.red(color) * factor)
        val g = Math.round(Color.green(color) * factor)
        val b = Math.round(Color.blue(color) * factor)
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255))
    }

    companion object {
        private const val COLOR_TRANSITION_DURATION = 250L
        private const val IMAGE_FADE_IN_OUT_DURATION = 125L
    }
}