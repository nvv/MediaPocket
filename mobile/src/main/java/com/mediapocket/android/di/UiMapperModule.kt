package com.mediapocket.android.di

import android.content.Context
import com.mediapocket.android.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.details.mapper.PodcastItemToEpisodeViewItemMapper
import com.mediapocket.android.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.episodes.viewitem.EpisodeDatabaseItemToViewItem
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Module
class UiMapperModule {

    @Provides
    @Singleton
    fun provideDownloadErrorToStringMapper(context: Context): DownloadErrorToStringMapper =
            DownloadErrorToStringMapper(context)

    @Provides
    @Singleton
    fun providesPodcastItemToEpisodeViewItemMapper(): PodcastItemToEpisodeViewItemMapper =
            PodcastItemToEpisodeViewItemMapper()


    @Provides
    @Singleton
    fun providesPodcastViewItemToPersistanceItemMapper(): PodcastViewItemToDatabaseItemMapper =
            PodcastViewItemToDatabaseItemMapper()


    @Provides
    @Singleton
    fun providesEpisodeDatabaseItemToViewItem(): EpisodeDatabaseItemToViewItem =
            EpisodeDatabaseItemToViewItem()


}