package com.mediapocket.android.di

import android.content.Context
import com.mediapocket.android.journeys.details.mapper.DownloadErrorToStringMapper
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
    fun provideDownloadErrorToStringMapper(context: Context): DownloadErrorToStringMapper {
        return DownloadErrorToStringMapper(context)
    }


}