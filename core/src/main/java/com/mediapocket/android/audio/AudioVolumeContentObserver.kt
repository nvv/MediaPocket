package com.mediapocket.android.audio

import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Handler

/**
 * @author Vlad Namashko
 */
class AudioVolumeContentObserver(
        handler: Handler,
        private val audioManager: AudioManager?,
        private val audioStreamType: Int,
        private val listener: OnAudioVolumeChangedListener?) : ContentObserver(handler) {

    private var lastVolume: Int = 0

    init {
        lastVolume = audioManager?.getStreamVolume(audioStreamType) ?: 0
    }

    /**
     * Depending on the handler this method may be executed on the UI thread
     */
    override fun onChange(selfChange: Boolean, uri: Uri) {
        if (audioManager != null && listener != null) {
            val maxVolume = audioManager.getStreamMaxVolume(audioStreamType)
            val currentVolume = audioManager.getStreamVolume(audioStreamType)
            if (currentVolume != lastVolume) {
                lastVolume = currentVolume
                listener.onAudioVolumeChanged(currentVolume, maxVolume)
            }
        }
    }

    override fun deliverSelfNotifications(): Boolean {
        return super.deliverSelfNotifications()
    }
}