package com.mediapocket.android.di

import com.mediapocket.android.api.retrofit.ItunesPodcastSearchService
import com.mediapocket.android.api.retrofit.ItunesTopPodcastService
import com.mediapocket.android.api.retrofit.RssService
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.repository.RssRepository
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