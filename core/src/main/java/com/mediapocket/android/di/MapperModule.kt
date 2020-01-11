package com.mediapocket.android.di

import com.mediapocket.android.core.download.mapper.FetchToDownloadManagerErrorMapper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Module
class MapperModule {

    @Provides
    @Singleton
    fun provideFetchToDownloadManagerErrorMapper(): FetchToDownloadManagerErrorMapper {
        return FetchToDownloadManagerErrorMapper()
    }

}