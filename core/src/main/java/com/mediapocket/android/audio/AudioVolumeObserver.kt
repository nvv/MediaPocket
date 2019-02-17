package com.mediapocket.android.audio

import android.content.Context
import android.media.AudioManager
import android.os.Handler


/**
 * @author Vlad Namashko
 */
class AudioVolumeObserver(private val context: Context) {
    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mAudioVolumeContentObserver: AudioVolumeContentObserver? = null

    fun register(audioStreamType: Int, listener: OnAudioVolumeChangedListener) {

        val handler = Handler()

        mAudioVolumeContentObserver = AudioVolumeContentObserver(
                handler,
                mAudioManager,
                audioStreamType,
                listener)

        context.getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                mAudioVolumeContentObserver)
    }

    fun unregister() {
        if (mAudioVolumeContentObserver != null) {
            context.getContentResolver().unregisterContentObserver(mAudioVolumeContentObserver)
            mAudioVolumeContentObserver = null
        }
    }
}

interface OnAudioVolumeChangedListener {

    fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int)
}