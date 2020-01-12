package com.mediapocket.android.journeys.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.mediapocket.android.R
import com.mediapocket.android.fragments.SimplePodcastFragment
import com.mediapocket.android.model.PodcastAdapterEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author Vlad Namashko
 */
class NetworkPodcastFragment : SimplePodcastFragment() {

    override fun getTitle() = arguments?.getString(ARG_NETWORK_TITLE) ?: ""

    override fun getLayoutId() = R.layout.subscribed_podcasts

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        arguments?.getString(ARG_NETWORK_ID)?.let { networkId ->
            model.getNetworkPodcasts.observe(this, Observer { result ->
                adapter.setItems(PodcastAdapterEntry.convert(result))
                adapter.notifyDataSetChanged()
            })

            GlobalScope.launch {
                model.getNetowrkPodcasts(networkId)
            }
        }

//        arguments?.getString(ARG_NETWORK_ID)?.let {
//            subscription.add(model.doLoadingAction { model.getNetowrkPodcasts(it) }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({ res ->
//                        adapter.setItems(PodcastAdapterEntry.convert(res))
//                        adapter.notifyDataSetChanged()
//                    }, { err ->
//                        err.printStackTrace() }))
//        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val ARG_NETWORK_ID = "arg_network_id"
        private const val ARG_NETWORK_TITLE = "arg_network_title"

        fun newInstance(id: String, title: String?): NetworkPodcastFragment {
            val fragment = NetworkPodcastFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_NETWORK_ID, id)
                putString(ARG_NETWORK_TITLE, title)
            }
            return fragment
        }

        const val TAG = "NetworkPodcastFragment"
    }
}