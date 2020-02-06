package com.mediapocket.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mediapocket.android.journeys.episodes.vm.EpisodesViewModel
import com.mediapocket.android.journeys.details.vm.PodcastDetailsViewModel
import com.mediapocket.android.journeys.discover.vm.PodcastViewModel
import com.mediapocket.android.journeys.discover.vm.SearchPodcastViewModel
import com.mediapocket.android.journeys.episodes.vm.DownloadedEpisodesViewModel
import com.mediapocket.android.journeys.episodes.vm.FavouritesEpisodesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(DownloadedEpisodesViewModel::class)
    protected abstract fun downloadedEpisodesViewModel(episodesViewModel: DownloadedEpisodesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavouritesEpisodesViewModel::class)
    protected abstract fun favouritesEpisodesViewModel(episodesViewModel: FavouritesEpisodesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PodcastDetailsViewModel::class)
    protected abstract fun podcastDetailsViewModel(podcastDetailsViewModel: PodcastDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PodcastViewModel::class)
    protected abstract fun podcastViewModel(podcastViewModel: PodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchPodcastViewModel::class)
    protected abstract fun searchPodcastViewModel(searchPodcastViewModel: SearchPodcastViewModel): ViewModel
}
