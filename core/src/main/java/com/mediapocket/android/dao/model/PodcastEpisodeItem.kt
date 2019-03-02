package com.mediapocket.android.dao.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Base64
import org.jetbrains.annotations.NotNull
import java.nio.charset.Charset

/**
 * @author Vlad Namashko
 */
@Entity(tableName = "podcast_episode_item")
data class PodcastEpisodeItem(@ColumnInfo(name = "state") var state: Int,
                              @ColumnInfo(name = "podcast_id") var podcastId: String?,
                              @ColumnInfo(name = "podcast_title") var podcastTitle: String?,
                              @ColumnInfo(name = "title") var title: String?,
                              @ColumnInfo(name = "description") var description: String?,
                              @ColumnInfo(name = "link") var link: String?,
                              @ColumnInfo(name = "download_date") var downloadDate: Long,
                              @ColumnInfo(name = "pub_date") var pubDate: String?,
                              @ColumnInfo(name = "length") var length: Long?,
                              @ColumnInfo(name = "favourite") var favourite: Boolean,
                              @ColumnInfo(name = "image_url") var imageUrl: String?,
                              @ColumnInfo(name = "download_id") var downloadId: Int,
                              @ColumnInfo(name = "local_path") var localPath: String?) {

    @ColumnInfo(name = "id")
    @PrimaryKey
    @NotNull
    var id: String = convertLinkToId(link)

    fun getMediaMetadataCompat() : MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, link)
                .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, podcastTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, pubDate)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, link)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, if (length == null) 0 else length!!)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, imageUrl)
                .build()
    }

    fun getMediaDescription(): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
                .setTitle(title)
                .setIconUri(Uri.parse(imageUrl))
                .setMediaId(link)
                .setExtras(getMediaExtras())
                .build()
    }

    private fun getMediaExtras(): Bundle {
        val bundle = Bundle()

        bundle.putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, podcastTitle)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_DATE, pubDate)
        bundle.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, if (length == null) 0 else length!!)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, link)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ART, imageUrl)

        return bundle
    }

    companion object {
        const val STATE_NONE = 0
        const val STATE_ADDED = 1
        const val STATE_DOWNLOADING = 2
        const val STATE_DOWNLOADED = 3
        const val STATE_PAUSED = 4
        const val STATE_ERROR = 5
        const val STATE_WAITING_FOR_NETWORK = 6

        fun convertLinkToId(link: String?) = Base64.encodeToString(link?.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT)
    }

}
