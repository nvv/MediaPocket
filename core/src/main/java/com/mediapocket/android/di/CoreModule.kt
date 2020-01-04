package com.mediapocket.android.di

import androidx.room.Room
import android.content.Context
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.Storage
import com.mediapocket.android.repository.PodcastEpisodeRepository
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
    fun provideAppDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, "media_pocket_database").build()

    @Provides
    @Singleton
    fun provideLocalStorage() = Storage(context)

    @Provides
    @Singleton
    fun providePodcastDownloadManager(repository: PodcastEpisodeRepository) =
            com.mediapocket.android.core.download.manager.PodcastDownloadManager(context, repository)
}