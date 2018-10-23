package com.mediapocket.android.di

import com.mediapocket.android.service.ItunesPodcastSearchService
import com.mediapocket.android.service.ItunesTopPodcastService
import com.mediapocket.android.service.RssService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Module
class ServiceModule {

    @Provides
    @Singleton
    fun provideTopPodcastsService() = ItunesTopPodcastService.create()

    @Provides
    @Singleton
    fun provideItunesPodcastSearchService() = ItunesPodcastSearchService.create()

    @Provides
    @Singleton
    fun provideRssService() = RssService.create()

}