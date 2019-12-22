package com.mediapocket.android.di

import com.mediapocket.android.App
import com.mediapocket.android.MainActivity
import com.mediapocket.android.PodcastService
import com.mediapocket.android.SplashScreenActivity
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.adapters.PodcastEpisodeAdapter
import com.mediapocket.android.viewmodels.EpisodesViewModel
import com.mediapocket.android.viewmodels.PodcastDetailsViewModel
import com.mediapocket.android.viewmodels.PodcastViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(
    modules = [
        AppModule::class,
        ActivityModule::class,
        ViewModelModule::class,
        CoreModule::class,
        RepositoryModule::class,
        RetrofitModule::class,
        AndroidSupportInjectionModule::class]
)
@Singleton
interface AppComponent {

    fun inject(application: App)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: App): Builder

        fun coreModule(module: CoreModule): Builder

        fun build(): AppComponent
    }

}