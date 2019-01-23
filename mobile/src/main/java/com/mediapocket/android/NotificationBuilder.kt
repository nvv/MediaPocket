package com.mediapocket.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Bitmap
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Html
import com.mediapocket.android.core.download.model.PodcastDownloadItem

/**
 * @author Vlad Namashko
 */
class NotificationBuilder(private val context: Context) {

    private val platformNotificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val stopPendingIntent =
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)

    private val rewind = NotificationCompat.Action(R.drawable.ic_rewind,
            context.getString(R.string.notification_rewind),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_REWIND))

    private val fastforward = NotificationCompat.Action(R.drawable.ic_fastforward,
            context.getString(R.string.notification_fastforward),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_FAST_FORWARD))

    private val next = NotificationCompat.Action(R.drawable.ic_next,
            context.getString(R.string.notification_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))

    private val prev = NotificationCompat.Action(R.drawable.ic_prev,
            context.getString(R.string.notification_prev),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))


    fun buildPlayerNotification(session: MediaSessionCompat, art : Bitmap? = null): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        val controller = session.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description

        val state = controller.playbackState

        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING ||
                state.state == PlaybackStateCompat.STATE_BUFFERING

        val mediaStyle = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setCancelButtonIntent(stopPendingIntent)
                .setMediaSession(session.sessionToken)
                .setShowActionsInCompactView(2)
                .setShowCancelButton(true)

        return builder.setContentIntent(controller?.sessionActivity)
                .setDeleteIntent(stopPendingIntent)
                // Add the metadata for the currently playing track
                .setContentTitle(description?.title)
                .setContentText(description?.subtitle)
//                .setSubText(description?.description)
                .setLargeIcon(art ?: description?.iconBitmap)


                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.exo_edit_mode_logo)

                // Add prev button
                .addAction(prev)

                // Add rewind button
                .addAction(rewind)

                // Add a pause button
                .addAction(NotificationCompat.Action(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                        context.getString(if (isPlaying) R.string.mr_controller_pause else R.string.mr_controller_play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))

                // Add fastforward button
                .addAction(fastforward)

                // Add next button
                .addAction(next)


                // Take advantage of MediaStyle features
                .setStyle(mediaStyle)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(controller.sessionActivity)
                .build()
    }

    fun buildDownloadNotification(items: List<PodcastDownloadItem>) : Notification {
        if (shouldCreateDownloadingChannel()) {
            createDownloadingChannel()
        }

        val max = 3
        var title = ""
        items.take(max).forEach { title += it.title + " <b>" + it.progress + "</b>% <br>" }
        if (items.size > max) {
            title += context.getString(R.string.n_more, items.size - max)
        }

        val builder = NotificationCompat.Builder(context, DOWNLOADING_CHANNEL)
        return builder
                .setContentTitle(context.getString(R.string.downloading_episodes))
                .setContentText(Html.fromHtml(title))
                .setStyle(NotificationCompat.BigTextStyle().bigText(Html.fromHtml(title)))
                .setOnlyAlertOnce(true)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
    }

    private fun shouldCreateNowPlayingChannel() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
            platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(NOW_PLAYING_CHANNEL,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW)
                .apply {
                    description = context.getString(R.string.notification_channel_description)
                }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }

    private fun shouldCreateDownloadingChannel() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !downloadingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun downloadingChannelExists() =
            platformNotificationManager.getNotificationChannel(DOWNLOADING_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDownloadingChannel() {
        val notificationChannel = NotificationChannel(DOWNLOADING_CHANNEL,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW)
                .apply {
                    description = context.getString(R.string.notification_channel_description)
                }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        const val NOW_PLAYING_CHANNEL: String = "com.mediapocket.android.NOW_PLAYING"
        const val NOW_PLAYING_NOTIFICATION: Int = 0xb400

        const val DOWNLOADING_CHANNEL: String = "com.mediapocket.android.DOWNLOADING"
        const val DOWNLOADING_NOTIFICATION: Int = 0xb401

    }
}