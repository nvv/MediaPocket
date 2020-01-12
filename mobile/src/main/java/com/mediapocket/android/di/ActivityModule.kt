package com.mediapocket.android.di

import com.mediapocket.android.MainActivity
import com.mediapocket.android.PodcastService
import com.mediapocket.android.SplashScreenActivity
import com.mediapocket.android.fragments.*
import com.mediapocket.android.journeys.details.PodcastDetailsFragment
import com.mediapocket.android.journeys.discover.DiscoverFragment
import com.mediapocket.android.journeys.discover.GenrePodcastsFragment
import com.mediapocket.android.journeys.discover.NetworkPodcastFragment
import com.mediapocket.android.journeys.discover.PodcastSearchFragment
import com.mediapocket.android.journeys.episodes.DownloadedEpisodesFragment
import com.mediapocket.android.journeys.episodes.EpisodesFragment
import com.mediapocket.android.journeys.episodes.FavouritesEpisodesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashScreenActivity(): SplashScreenActivity

    @ContributesAndroidInjector
    abstract fun contributePodcastService(): PodcastService

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFragment(): DiscoverFragment

    @ContributesAndroidInjector
    abstract fun contributeDownloadedEpisodesFragment(): DownloadedEpisodesFragment

    @ContributesAndroidInjector
    abstract fun contributeEpisodesFragment(): EpisodesFragment

    @ContributesAndroidInjector
    abstract fun contributeFavouritesEpisodesFragment(): FavouritesEpisodesFragment

    @ContributesAndroidInjector
    abstract fun contributeGenrePodcastsFragment(): GenrePodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeNetworkPodcastFragment(): NetworkPodcastFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastDetailsFragment(): PodcastDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastSearchFragment(): PodcastSearchFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastSubscriptionFragment(): PodcastSubscriptionFragment

}