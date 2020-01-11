package com.mediapocket.android.di

import com.mediapocket.android.App
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
        MapperModule::class,
        UiMapperModule::class,
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