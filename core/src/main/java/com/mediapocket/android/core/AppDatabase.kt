package com.mediapocket.android.core

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mediapocket.android.dao.EpisodesDao
import com.mediapocket.android.dao.SubscribedPodcastDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.dao.model.SubscribedPodcast

/**
 * @author Vlad Namashko
 */
@Database(entities = [
    SubscribedPodcast::class,
    PodcastEpisodeItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscribedPodcastDao(): SubscribedPodcastDao

    abstract fun podcastEpisodeItemDao(): EpisodesDao

}