package com.mediapocket.android

import android.app.Activity
import android.app.Application
import android.app.Service
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.di.CoreModule
import com.mediapocket.android.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()

        DependencyLocator.initInstance(this)

        DaggerAppComponent.builder()
                .application(this)
                .coreModule(CoreModule(applicationContext))
                .build()
                .inject(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? = dispatchingAndroidInjector

    override fun serviceInjector(): AndroidInjector<Service> = dispatchingServiceInjector
}