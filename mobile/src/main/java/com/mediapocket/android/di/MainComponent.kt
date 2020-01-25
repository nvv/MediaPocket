package com.mediapocket.android.di

import com.mediapocket.android.journeys.episodes.adapter.LocalEpisodesAdapter
import com.mediapocket.android.journeys.details.adapter.PodcastEpisodeAdapter
import dagger.Component
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
@Component(modules = [(CoreModule::class)])
interface MainComponent {

    fun inject(adapter : PodcastEpisodeAdapter)

    fun inject(adapter: LocalEpisodesAdapter)

}