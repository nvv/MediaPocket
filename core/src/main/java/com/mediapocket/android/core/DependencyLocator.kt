package com.mediapocket.android.core

import androidx.room.Room
import android.content.Context
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.di.*
import com.mediapocket.android.utils.ViewUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Vlad Namashko
 */
class DependencyLocator private constructor(val context: Context) {

    val coreModule: CoreModule = CoreModule(context)

    val serviceComponent: ServiceComponent = DaggerServiceComponent.builder().build()
    val coreComponent: CoreComponent = DaggerCoreComponent.builder().coreModule(coreModule).build()
    //val database = Room.databaseBuilder(context, AppDatabase::class.java, "media_pocket_database").build()
    //val podcastDownloadManager = PodcastDownloadManager(context, database)

    init {
        ViewUtils.init(context)
    }

    companion object {

        @Volatile private lateinit var INSTANCE: DependencyLocator
        private val initialized = AtomicBoolean()

        fun initInstance(context: Context) {
            if (!initialized.getAndSet(true)) {
                INSTANCE = buildDependencyLocator(context)
            }
        }

        fun getInstance() = INSTANCE

        private fun buildDependencyLocator(context: Context) = DependencyLocator(context)

    }
}