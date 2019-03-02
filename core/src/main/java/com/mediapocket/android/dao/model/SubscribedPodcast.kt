package com.mediapocket.android.dao.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediapocket.android.model.PodcastDiscoverResult

/**
 * @author Vlad Namashko
 */
@Entity(tableName = "podcast_subscription")
data class SubscribedPodcast(@PrimaryKey var id: String,
                             @ColumnInfo(name = "title") var title: String,
                             @ColumnInfo(name = "logo") var logo: String,
                             @ColumnInfo(name = "feed_url") var feedUrl: String,
                             @ColumnInfo(name = "primary_genre") var primaryGenre: String?,
                             @ColumnInfo(name = "genres") var genres: String?,
                             @ColumnInfo(name = "artist_id") var artistId: String?,
                             @ColumnInfo(name = "artist_name") var artistName: String?)