package com.mediapocket.android

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject

/**
 * @author Vlad Namashko
 */
class MediaSessionConnection(context: Context) {

    lateinit var mediaController: MediaControllerCompat

    private var subject: BehaviorSubject<Unit> = BehaviorSubject.create()

    private val mediaControllerCallbacks = mutableListOf<MediaControllerCompat.Callback>()

    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            // Get the token for the MediaSession
            val token = mediaBrowser.sessionToken

            mediaController = MediaControllerCompat(context, token).apply {
                registerCallback(mediaControllerCallback)

                subject.onNext(Unit)
            }
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    val mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(context,
            ComponentName(context, PodcastService::class.java),
            mediaBrowserConnectionCallback, null)
            .apply { connect() }

    fun connected(consumer: Consumer<Unit>, onError: Consumer<in Throwable>): Disposable {
        return subject.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer, onError)
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            mediaControllerCallbacks.forEach { it -> it.onPlaybackStateChanged(state) }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaControllerCallbacks.forEach { it -> it.onMetadataChanged(metadata) }
        }
    }

    fun registerMediaControllerCallback(callback: MediaControllerCompat.Callback) {
        mediaControllerCallbacks.add(callback)
    }

    fun unregisterMediaControllerCallback(callback: MediaControllerCompat.Callback) {
        mediaControllerCallbacks.remove(callback)
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MediaSessionConnection? = null

        fun getInstance(context: Context) =
                instance ?: synchronized(this) {
                    instance ?: MediaSessionConnection(context)
                            .also { instance = it }
                }
    }
}