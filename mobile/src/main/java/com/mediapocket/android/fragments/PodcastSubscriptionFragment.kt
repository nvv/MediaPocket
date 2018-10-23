package com.mediapocket.android.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.viewmodels.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * @author Vlad Namashko
 */
class PodcastSubscriptionFragment : SimplePodcastFragment() {

    override fun getTitle() = DependencyLocator.getInstance().context.getString(R.string.subscription)

    override fun getLayoutId() = R.layout.subscribed_podcasts

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        subscription.add(model.doLoadingAction { model.getSubscriptions() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            res ->
                    adapter.setItems(PodcastAdapterEntry.convert(res))
                    adapter.notifyDataSetChanged()
        })

        return view
    }

    companion object {
        fun newInstance(): PodcastSubscriptionFragment {
            return PodcastSubscriptionFragment()
        }

        const val TAG = "PodcastSubscriptionFragment"
    }
}