package com.mediapocket.android.di

import android.content.Context
import com.mediapocket.android.core.DependencyLocator
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Vlad Namashko
 */
object MainComponentLocator {

    val mainComponent: MainComponent = DaggerMainComponent.builder().coreModule(DependencyLocator.getInstance().coreModule).build()

}