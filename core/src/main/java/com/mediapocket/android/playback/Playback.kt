package com.mediapocket.android.playback

import android.support.v4.media.session.MediaSessionCompat

/**
 *
 *
 * @author Vlad Namashko
 */
interface Playback {

    fun stop()

    fun isPlaying(): Boolean

    fun getCurrentStreamPosition(): Long

    fun play(item: MediaSessionCompat.QueueItem)

    fun pause()

    fun seekTo(position: Long)

}