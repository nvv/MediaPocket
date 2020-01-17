package com.mediapocket.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.SubscriptionsLookupResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Vlad Namashko
 */
class PodcastSubscriptionFragment : SimplePodcastFragment() {

    override fun getTitle() = DependencyLocator.getInstance().context.getString(R.string.subscription)

    override fun getLayoutId() = R.layout.subscribed_podcasts

    var result : SubscriptionsLookupResult? = null

    private lateinit var items: View
    private lateinit var emptyFrame: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)?.apply {
            items = findViewById(R.id.items)
            emptyFrame = findViewById(R.id.empty_frame)
        }

        if (result != null) {
            onDataLoaded()
        } else {
            subscription.add(model.doLoadingAction { model.getSubscriptions() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { res ->
                        result = res
                        onDataLoaded()
                    })
        }

        return view
    }

    private fun onDataLoaded() {
        val hasData = result != null && !result?.items.isNullOrEmpty()
        if (hasData) {
            adapter.setItems(PodcastAdapterEntry.convert(result))
            adapter.notifyDataSetChanged()
        }

        items.visibility = if (hasData) View.VISIBLE else View.GONE
        emptyFrame.visibility = if (!hasData) View.VISIBLE else View.GONE
    }

    override fun hasBackNavigation() = false

    companion object {
        fun newInstance(): PodcastSubscriptionFragment {
            return PodcastSubscriptionFragment()
        }

        const val TAG = "PodcastSubscriptionFragment"
    }
}