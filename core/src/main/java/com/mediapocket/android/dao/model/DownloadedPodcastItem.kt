package com.mediapocket.android.dao.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.util.Base64
import org.jetbrains.annotations.NotNull
import java.nio.charset.Charset

/**
 * @author Vlad Namashko
 */
@Entity(tableName = "downloaded_podcast_item")
data class DownloadedPodcastItem(@ColumnInfo(name = "state") var state: Int,
                                 @ColumnInfo(name = "podcast_id") var podcastId: String?,
                                 @ColumnInfo(name = "podcast_title") var podcastTitle: String?,
                                 @ColumnInfo(name = "title") var title: String?,
                                 @ColumnInfo(name = "description") var description: String?,
                                 @ColumnInfo(name = "link") var link: String?,
                                 @ColumnInfo(name = "pub_date") var pubDate: String?,
                                 @ColumnInfo(name = "length") var length: Long?,
                                 @ColumnInfo(name = "image_url") var imageUrl: String?,
                                 @ColumnInfo(name = "local_path") var localPath: String?) {

    @ColumnInfo(name = "id")
    @PrimaryKey
    @NotNull
    var id: String = convertLinkToId(link)

    companion object {
        const val STATE_ADDED = 0
        const val STATE_DOWNLOADING = 1
        const val STATE_DOWNLOADED = 2
        const val STATE_PAUSED = 3

        fun convertLinkToId(link: String?) = Base64.encodeToString(link?.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT)
    }

}
