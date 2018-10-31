package com.mediapocket.android.fragments

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.view.PodcastDetailsView
import com.mediapocket.android.viewmodels.PodcastDetailsViewModel
import io.reactivex.disposables.CompositeDisposable


/**
 * @author Vlad Namashko
 */
class PodcastDetailsFragment : BaseFragment() {

    private lateinit var model: PodcastDetailsViewModel
    public var podcast: PodcastAdapterEntry? = null

    private lateinit var podcastView: PodcastDetailsView

    private val subscription: CompositeDisposable = CompositeDisposable()

    private var dataLoaded = false

    private var subscribe: FloatingActionButton? = null
    private var subscribeMenu: MenuItem? = null
    private var openWebSiteMenu: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.podcast_fragmnet_view, container, false)

        podcastView = view.findViewById(R.id.podcast_view)
        if (podcastView.hasOptionsMenu()) {
            subscribeMenu = podcastView.getMenu()?.findItem(R.id.subscribe)
            openWebSiteMenu = podcastView.getMenu()?.findItem(R.id.open_web_site)
        }

        podcast = arguments?.getParcelable(ARG_PODCAST)
        model = ViewModelProviders.of(this).get(PodcastDetailsViewModel::class.java)

        subscribe = view.findViewById(R.id.subscribe)
        podcast?.let {
            subscription.add(model.load(it).subscribe { podcastDetails ->
                podcastDetails.artwork()?.let {
                    dataLoaded = true
                    podcastView.loadLogo(it)
                }

                podcastView.fullDataFetched(podcastDetails)

                subscribe?.let {

                    var subscribed = false
                    subscription.add(model.isSubscribed(podcast!!.id()).subscribe { isSubscribed ->
                        subscribed = isSubscribed
                        syncSubscribeButton(subscribed)
                    })

                    it.setOnClickListener {

                        val icon = resources.getDrawable(if (subscribed) R.drawable.ic_unsubscribe_animated else R.drawable.ic_subscribe_animated, null) as AnimatedVectorDrawable
                        subscribe?.setImageDrawable(icon)
                        icon.start()

                        subscription.add(model.subscribe(podcast!!, podcastDetails).subscribe { _ ->
                            subscribed = !subscribed

                            val snackbar = Snackbar.make(view.findViewById(R.id.podcast_details),
                                    getString(if (subscribed) R.string.action_subscribed else R.string.action_unsubscribed, podcast?.title()), Snackbar.LENGTH_SHORT)
                            snackbar.setAction(getString(R.string.action_undo)) {
                                subscription.add(model.subscribe(podcast!!, podcastDetails).subscribe {value ->
                                    subscribed = value
                                    syncSubscribeButton(subscribed)
                                })
                            }
                            snackbar.show()
                            syncSubscribeButton(subscribed, true)
                        })
                    }
                }

                subscription.add(model.loadFeed(podcastDetails).subscribe({rss ->
                    podcastView.feedLoaded(rss)

                    openWebSiteMenu?.setOnMenuItemClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rss.webSite())))
                        false
                    }
                }, {err ->
                    err.printStackTrace()
                }))
            })
        }

        return view
    }

    private fun syncSubscribeButton(subscribed: Boolean, manualyInvoked : Boolean = false) {
        if (!manualyInvoked) {
            subscribe?.setImageResource(if (subscribed) R.drawable.ic_checked else R.drawable.ic_add)
        }
        subscribeMenu?.setTitle(if (subscribed) R.string.unsubscribe else R.string.subscribe)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podcastView.fragmentCreated(podcast)
    }

    companion object {

        private val ARG_PODCAST = "arg_podcast"

        fun newInstance(podcast: PodcastAdapterEntry): PodcastDetailsFragment {
            val fragment = PodcastDetailsFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putParcelable(ARG_PODCAST, podcast)
            return fragment
        }

        val TAG = "PodcastDetailsFragment"
    }

    override fun getTitle() = ""

    override fun hasNavigation() = false

    override fun hasBackNavigation() = true
}