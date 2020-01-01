package com.mediapocket.android

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.*
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.extensions.albumArt
import com.mediapocket.android.extensions.displayIconUriString
import com.mediapocket.android.extensions.from
import com.mediapocket.android.extensions.id
import com.mediapocket.android.playback.PlaybackUnit
import com.mediapocket.android.playback.model.PlayableItem
import com.mediapocket.android.repository.RssRepository
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Vlad Namashko
 */
class PodcastService : MediaBrowserServiceCompat() {

    private val TAG = "PodcastService"

    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

    private lateinit var mSession: MediaSessionCompat
    private var stateBuilder: PlaybackStateCompat.Builder =
            PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_REWIND or
                    PlaybackStateCompat.ACTION_FAST_FORWARD or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )

    private lateinit var playback: PlaybackUnit

    private lateinit var notificationBuilder: NotificationBuilder
    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var mediaController: MediaControllerCompat

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    private var isForegroundService = false

    @set:Inject
    lateinit var database: AppDatabase

    @set:Inject
    lateinit var downloadManager: PodcastDownloadManager

    @set:Inject
    lateinit var rssRepository: RssRepository

    private val subscription = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        DependencyLocator.initInstance(this)
        AndroidInjection.inject(this)

//        MainComponentLocator.mainComponent.inject(this)

        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        mSession = MediaSessionCompat(this, TAG)

        // Enable callbacks from MediaButtons and TransportControls
        mSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS or FLAG_HANDLES_QUEUE_COMMANDS or FLAG_HANDLES_TRANSPORT_CONTROLS)

        mSession.setPlaybackState(stateBuilder.build())
        mSession.setSessionActivity(sessionActivityPendingIntent)

//        mSession.setCallback(MediaSessionCallback())

        mSession.isActive = true
        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver = BecomingNoisyReceiver(context = this, sessionToken = mSession.sessionToken)

        // MySessionCallback() has methods that handle callbacks from a media controller
//        mSession.setCallback(mSessionCallback)

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mSession.sessionToken

        mediaController = MediaControllerCompat(this, mSession.sessionToken).also {
            it.registerCallback(MediaControllerCompatCallback())
        }

        playback = PlaybackUnit(this, mSession)
        notificationBuilder = NotificationBuilder(this)

        subscription.add(downloadManager.subscribeForDatabaseChanges(Consumer {
            if (playback.currentMediaId == PlayableItem.MY_MEDIA_ID_DOWNLOADED) {
                MediaSessionConnection.getInstance(applicationContext).mediaBrowser.apply {
                    unsubscribe(PlayableItem.MY_MEDIA_ID_DOWNLOADED)
                    subscribe(PlayableItem.MY_MEDIA_ID_DOWNLOADED, object : MediaBrowserCompat.SubscriptionCallback() {

                        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
                            //currentMediaId = parentId

                            //playback.initWithFeedItems()



                            mediaController.sendCommand(PlaybackUnit.COMMAND_RENEW_PLAYLIST, null, null)
                        }
                    })
                }
            }
        }, Schedulers.io()))

/*
        ContextCompat.startForegroundService(
                this@PodcastService,
                Intent(this@PodcastService, PodcastService::class.java))
*/
    }

    override fun onDestroy() {
        super.onDestroy()
        mSession.run {
            isActive = false
            release()
        }
        subscription.clear()
    }

    override fun onLoadChildren(parentMediaId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        //  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null)
            return
        }

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentMediaId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            result.detach()
            GlobalScope.launch {
                result.sendResult(loadPlaylist(parentMediaId))
            }

//            subscription.add(loadPlaylist(parentMediaId).observeOn(AndroidSchedulers.mainThread()).subscribe { items ->
//                result.sendResult(items)
//            })
        }
    }

    private suspend fun loadPlaylist(mediaId: String): List<MediaBrowserCompat.MediaItem> {
        return if (mediaId == PlayableItem.MY_MEDIA_ID_DOWNLOADED) {
            GlobalScope.async {
                val items = database.podcastEpisodeItemDao().getDownloaded()
                playback.initWithLocalEpisodes(mediaId, items)
            }.await()

        } else {
            GlobalScope.async {
                val rss = rssRepository.loadRss(mediaId)
                playback.initWithFeedItems(mediaId, rss.items())
            }.await()
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        return if (/*allowBrowsing(clientPackageName, clientUid)*/ false) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    private inner class MediaControllerCompatCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

            val updatedState = state?.state ?: return

            // Skip building a notification when state is "none".
            val notification = if (updatedState != PlaybackStateCompat.STATE_NONE) {
                notificationBuilder.buildPlayerNotification(mSession, checkLoadedArt())
            } else {
                null
            }

            when (updatedState) {
                PlaybackStateCompat.STATE_BUFFERING -> {
                    becomingNoisyReceiver.register()
                    startForeground(NotificationBuilder.NOW_PLAYING_NOTIFICATION, notification)
                    isForegroundService = true
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()
                    startForeground(NotificationBuilder.NOW_PLAYING_NOTIFICATION, notification)

                    isForegroundService = true
                }
//                PlaybackStateCompat.STATE_NONE -> {
//                    System.out.println(">>> STATE_NONE")
//
//                }
                else -> {
                    becomingNoisyReceiver.unregister()


                    if (isForegroundService) {
                        stopForeground(false)
//
                        if (notification != null) {
                            notificationManager.notify(NotificationBuilder.NOW_PLAYING_NOTIFICATION, notification)
                        } else {
                            stopForeground(true)
                        }
                        isForegroundService = false
                    }
                }
            }
        }

        fun buildNotification() {
            startForeground(NotificationBuilder.NOW_PLAYING_NOTIFICATION, notificationBuilder.buildPlayerNotification(mSession, checkLoadedArt()))
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (mSession.controller.playbackState.state != PlaybackStateCompat.STATE_NONE) {
                loadArt()
                buildNotification()
            }
        }

        private fun checkLoadedArt(): Bitmap? =
                if (mSession.controller.metadata?.id == currentItem && mSession.controller.metadata?.albumArt == null && currentArt != null) currentArt
                else null

        private fun loadArt() {
//            if (mSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                if (mSession.controller.metadata?.id == currentItem && mSession.controller.metadata?.albumArt == null && currentArt != null) {
                    updateSession(currentArt)
                }

                if (mSession.controller.metadata?.albumArt == null) {

                    val imageUrl = mSession.controller.metadata?.displayIconUriString
                    imageUrl?.let {
                        Single.fromCallable {
                            Glide.with(this@PodcastService)
                                    .applyDefaultRequestOptions(RequestOptions()
                                            .fallback(R.drawable.exo_icon_play)
                                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                                    .asBitmap()
                                    .load(imageUrl)
                                    .submit()
                                    .get()
                        }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { art ->
                                    currentArt = art
                                    currentItem = mSession.controller.metadata.id
                                    updateSession(art)
                                }
                    }
                }
//            }
        }

        private fun updateSession(art: Bitmap? = null) {
            val currentMetadata = mSession.controller.metadata
            mSession.setMetadata(MediaMetadataCompat.Builder().from(currentMetadata).apply {
                albumArt = art
            }.build())
        }

//        private val NOTIFICATION_LARGE_ICON_SIZE = 144
        private var currentItem:String? = null
        private var currentArt:Bitmap? = null
    }

    private class BecomingNoisyReceiver(private val context: Context,
                                        sessionToken: MediaSessionCompat.Token) : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val controller = MediaControllerCompat(context, sessionToken)

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }
}