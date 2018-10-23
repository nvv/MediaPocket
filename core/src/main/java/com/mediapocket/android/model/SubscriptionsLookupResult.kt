package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.dao.model.SubscribedPodcast

/**
 * @author Vlad Namashko
 */
class SubscriptionsLookupResult(val items: List<SubscribedPodcast>) : PodcastDiscoverResult {

    override fun title() = DependencyLocator.getInstance().context.getString(R.string.subscription)

}