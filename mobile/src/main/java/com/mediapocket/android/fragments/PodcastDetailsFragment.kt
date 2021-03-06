package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadNetworkItemsEvent
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
    private var moreFromAuthor: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.podcast_fragmnet_view, container, false)

        podcastView = view.findViewById(R.id.podcast_view)
        if (podcastView.hasOptionsMenu()) {
            subscribeMenu = podcastView.getMenu()?.findItem(R.id.subscribe)
            openWebSiteMenu = podcastView.getMenu()?.findItem(R.id.open_web_site)
            moreFromAuthor = podcastView.getMenu()?.findItem(R.id.more_from_author)
        }

        podcast = arguments?.getParcelable(ARG_PODCAST)
        model = ViewModelProviders.of(this).get(PodcastDetailsViewModel::class.java)

        subscribe = view.findViewById(R.id.subscribe)
        podcast?.let {
            subscription.add(model.load(it).subscribe { podcastDetails ->
                podcastDetails.artwork?.let {
                    dataLoaded = true
                    podcastView.loadLogo(it)
                }

                moreFromAuthor?.isVisible = podcastDetails?.authorId != null
                podcastView.fullDataFetched(podcastDetails)

                subscribe?.let {

                    var subscribed = false
                    subscription.add(model.isSubscribed(podcast!!.id()).subscribe { isSubscribed ->
                        subscribed = isSubscribed
                        syncSubscribeButton(subscribed)
                    })

                    it.setOnClickListener {

                        val stateSet = intArrayOf(android.R.attr.state_checked * if (!subscribed) 1 else -1)
                        subscribe?.setImageState(stateSet, true)

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
                    podcastView.feedLoaded(rss, podcast?.id())

                    openWebSiteMenu?.setOnMenuItemClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rss.webSite())))
                        false
                    }

                    moreFromAuthor?.setOnMenuItemClickListener {
                        podcastDetails?.authorId?.let {
                            RxBus.default.postEvent(LoadNetworkItemsEvent(it, podcastDetails.authorName))
                        }
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
            val stateSet = intArrayOf(android.R.attr.state_checked * if (subscribed) 1 else -1)
            subscribe?.setImageState(stateSet, true)
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