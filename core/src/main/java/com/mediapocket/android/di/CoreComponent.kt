package com.mediapocket.android.di

import com.mediapocket.android.core.download.PodcastDownloadManager
import dagger.Component
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
@Component(modules = [(CoreModule::class)])
interface CoreComponent {

    fun inject(manager: PodcastDownloadManager)

}