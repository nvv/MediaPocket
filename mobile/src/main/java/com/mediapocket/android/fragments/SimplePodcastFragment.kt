package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.mediapocket.android.discover.vm.PodcastViewModel

/**
 * @author Vlad Namashko
 */
abstract class SimplePodcastFragment : BasePodcastFragment() {

    override fun isLoading() = model.isLoading

    override fun hasNavigation() = true

    override fun hasBackNavigation() = true

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