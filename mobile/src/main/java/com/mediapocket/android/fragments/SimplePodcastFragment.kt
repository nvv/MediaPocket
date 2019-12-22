package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.mediapocket.android.viewmodels.PodcastViewModel

/**
 * @author Vlad Namashko
 */
abstract class SimplePodcastFragment : BasePodcastFragment() {

    override fun hasNavigation() = true

    override fun hasBackNavigation() = true

    override fun loading() = model.loading()

    protected lateinit var model: PodcastViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel::class.java)
    }

    override fun onDetach() {
        super.onDetach()
        subscription.dispose()
    }
}