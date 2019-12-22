package com.mediapocket.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mediapocket.android.viewmodels.EpisodesViewModel
import com.mediapocket.android.viewmodels.PodcastDetailsViewModel
import com.mediapocket.android.viewmodels.PodcastViewModel
import com.mediapocket.android.viewmodels.SearchPodcastViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(EpisodesViewModel::class)
    protected abstract fun episodesViewModel(episodesViewModel: EpisodesViewModel): ViewModel

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
