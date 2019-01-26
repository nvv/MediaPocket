package com.mediapocket.android.di

import android.arch.persistence.room.Room
import android.content.Context
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.service.ItunesTopPodcastService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Module
class CoreModule(private val context: Context) {

    @Provides
    @Singleton
    fun providePodcastDownloadManager() = PodcastDownloadManager(context)

    @Provides
    @Singleton
    fun provideAppDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, "media_pocket_database").build()
}