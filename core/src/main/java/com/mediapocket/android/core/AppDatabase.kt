package com.mediapocket.android.core

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.mediapocket.android.dao.DownloadedPodcastItemDao
import com.mediapocket.android.dao.SubscribedPodcastDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.dao.model.SubscribedPodcast

/**
 * @author Vlad Namashko
 */
@Database(entities = [(SubscribedPodcast::class), (PodcastEpisodeItem::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscribedPodcastDao(): SubscribedPodcastDao

    abstract fun downloadedPodcastItemDao(): DownloadedPodcastItemDao

}