package com.mediapocket.android.di

import com.mediapocket.android.MainActivity
import com.mediapocket.android.PodcastService
import com.mediapocket.android.SplashScreenActivity
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.adapters.PodcastEpisodeAdapter
import com.mediapocket.android.viewmodels.DownloadedEpisodesViewModel
import com.mediapocket.android.viewmodels.PodcastDetailsViewModel
import com.mediapocket.android.viewmodels.PodcastViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
@Component(modules = [(CoreModule::class)])
interface MainComponent {

    fun inject(adapter : PodcastEpisodeAdapter)

    fun inject(activity : MainActivity)

    fun inject(detailsViewModel: PodcastDetailsViewModel)

    fun inject(downloadedEpisodesViewModel: DownloadedEpisodesViewModel)

    fun inject(podcastViewModel: PodcastViewModel)

    fun inject(adapter: DownloadedEpisodesAdapter)

    fun inject(service: PodcastService)

    fun inject(splash: SplashScreenActivity)

}