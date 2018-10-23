package com.mediapocket.android.core

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.mediapocket.android.dao.SubscribedPodcastDao
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.model.PodcastAdapterEntry

/**
 * @author Vlad Namashko
 */
@Database(entities = arrayOf(SubscribedPodcast::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscribedPodcastDao(): SubscribedPodcastDao
}