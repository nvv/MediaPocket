package com.mediapocket.android.di

import com.mediapocket.android.MainActivity
import com.mediapocket.android.PodcastService
import com.mediapocket.android.SplashScreenActivity
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.adapters.PodcastEpisodeAdapter
import com.mediapocket.android.viewmodels.EpisodesViewModel
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

    fun inject(adapter: DownloadedEpisodesAdapter)

}