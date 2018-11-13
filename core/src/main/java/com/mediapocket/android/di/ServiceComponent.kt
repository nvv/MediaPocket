package com.mediapocket.android.di

import com.mediapocket.android.service.ItunesPodcastRepository
import com.mediapocket.android.service.RssRepository
import dagger.Component
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
@Component(modules = [(ServiceModule::class)])
interface ServiceComponent {

    fun inject(repo: ItunesPodcastRepository)

    fun inject(repo: RssRepository)

}