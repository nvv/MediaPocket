package com.mediapocket.android.dao.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.mediapocket.android.model.PodcastDiscoverResult

/**
 * @author Vlad Namashko
 */
@Entity(tableName = "podcast_subscription")
data class SubscribedPodcast(@PrimaryKey var id: String,
                             @ColumnInfo(name = "title") var title: String,
                             @ColumnInfo(name = "logo") var logo: String,
                             @ColumnInfo(name = "feed_url") var feedUrl: String)