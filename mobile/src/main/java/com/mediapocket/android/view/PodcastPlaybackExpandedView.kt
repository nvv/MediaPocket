package com.mediapocket.android.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.SwitchPodcastPlayerModeEvent
import com.mediapocket.android.events.VolumeLevelKeyEvent
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.playback.LocalPlayback.Companion.ARG_PLAYBACK_RATE
import com.mediapocket.android.playback.LocalPlayback.Companion.COMMAND_SET_PLAYBACK_RATE
import com.mediapocket.android.utils.ViewUtils
import io.reactivex.disposables.CompositeDisposable


/**
 * @author Vlad Namashko
 */
class PodcastPlaybackExpandedView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PodcastPlaybackView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private val title: TextView
    private val subTitle: TextView
    private val seekBarView: MediaSeekBarView
    private val next: View
    private val prev: View
    private val rewind: ImageView

    private val volumeControl: SeekBar

    private var disposable: CompositeDisposable? = null

    private val gestureDetector: GestureDetectorCompat

    init {
        findViewById<View>(R.id.close).setOnClickListener { RxBus.default.postEvent(SwitchPodcastPlayerModeEvent.close()) }

        title = findViewById(R.id.title)
        subTitle = findViewById(R.id.subtitle)
        seekBarView = findViewById(R.id.media_seek_bar_view)
        next = findViewById(R.id.next)
        prev = findViewById(R.id.prev)
        rewind = findViewById(R.id.rewind)

        next.setOnClickListener {
            mediaConnection?.mediaController?.transportControls?.skipToNext()
        }

        prev.setOnClickListener {
            mediaConnection?.mediaController?.transportControls?.skipToPrevious()
        }

        rewind.setOnClickListener{
            rotate(rewind, 0f, -90f)
            mediaConnection?.mediaController?.transportControls?.rewind()
        }

        volumeControl = findViewById(R.id.volume_level)

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
            if (volumeControl.progress != it.volume) {
                volumeControl.progress = it.volume
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
        seekBarView.setMediaController(mediaConnection.mediaController)

        mediaConnection.mediaController.playbackInfo?.let {
            val maxVolume = it.maxVolume
            volumeControl.max = maxVolume
            volumeControl.progress = it.currentVolume
            volumeControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaConnection.mediaController.setVolumeTo(progress, 0)
                    }
                }
            })

            findViewById<View>(R.id.volume_min).setOnClickListener {
                mediaConnection.mediaController.setVolumeTo(0, 0)
            }

            findViewById<View>(R.id.volume_max).setOnClickListener {
                mediaConnection.mediaController.setVolumeTo(maxVolume, 0)
            }

            val playbackSpeed = findViewById<TextView>(R.id.playback_speed)
            playbackSpeed.tag = 10
            playbackSpeed.setOnClickListener {
                val popDialog = AlertDialog.Builder(context)
                val seek = SeekBar(context)
                seek.setPadding(seek.paddingLeft, ViewUtils.getDimensionSize(16).toInt(), seek.paddingRight, seek.paddingBottom)
                seek.max = 40
                seek.progress = playbackSpeed.tag as Int

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
                    playbackSpeed.text = "%sx".format(calculateRate(seek.progress))
                    playbackSpeed.tag = seek.progress
                    dialog.dismiss()
                }
                popDialog.setNegativeButton(R.string.btn_cancel) { dialog, which ->
                    val speed = calculateRate(playbackSpeed.tag as Int)
                    playbackSpeed.text = "%sx".format(speed)

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
        var rate = 1.0f
        if (progress < 10) {
            rate = 1 - (10 - progress) / 20f
        } else if (progress > 10) {
            rate = 1 + (progress - 10) / 20f
        }
        return rate
    }

    override fun getMediaControllerCallback() = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {

            if (isPlaying != state.isPlaying && state.state != PlaybackStateCompat.STATE_BUFFERING) {
                isPlaying = state.isPlaying
                playPause.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.description?.let {
                it.iconBitmap?.let {
                    episodeLogo.setImageBitmap(it)
                }

                title.text = it.title
                subTitle.text = it.subtitle

                it.extras?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            }
        }

    }

}