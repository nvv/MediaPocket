package com.mediapocket.android.di

import com.mediapocket.android.service.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideItunesPodcastRepository(
            topPodcastService: ItunesTopPodcastService,
            searchPodcastService: ItunesPodcastSearchService
    ): ItunesPodcastRepository {
        return ItunesPodcastRepository(topPodcastService, searchPodcastService)
    }

    @Provides
    @Singleton
    fun provideRssRepository(rssService: RssService): RssRepository {
        return RssRepository(rssService)
    }

}