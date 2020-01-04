package com.mediapocket.android.journeys.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mediapocket.android.R
import com.mediapocket.android.adapters.PodcastEpisodeAdapter
import com.mediapocket.android.fragments.BaseFragment
import com.mediapocket.android.journeys.details.view.PodcastDetailsView
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.journeys.details.vm.PodcastDetailsViewModel
import com.mediapocket.android.model.PodcastAdapterEntry
import javax.inject.Inject


/**
 * @author Vlad Namashko
 */
class PodcastDetailsFragment : BaseFragment() {

    private lateinit var model: PodcastDetailsViewModel
    var podcast: PodcastAdapterEntry? = null

    private lateinit var podcastView: PodcastDetailsView

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
        model = ViewModelProviders.of(this, viewModelFactory).get(PodcastDetailsViewModel::class.java)

        subscribe = view.findViewById(R.id.subscribe)
        podcast?.let { podcast ->

            model.loadPodcast.observe(this, Observer { podcastDetails ->
                podcastDetails.artwork?.let {
                    dataLoaded = true
                    podcastView.loadLogo(it)
                }

                moreFromAuthor?.isVisible = podcastDetails?.authorId != null
                podcastView.fullDataFetched(podcastDetails)

                subscribe?.let {

                    model.isSubscribed.observe(this, Observer { isSubscribed ->
                        syncSubscribeButton(isSubscribed)
                    })

                    model.isSubscribed(podcast.id())

                    it.setOnClickListener {
                        model.subscribe(podcast, podcastDetails, explicitlyInvoked = true)
                    }

                    model.showUndo.observe(this, Observer {isSubscribed ->
                        val snackbar = Snackbar.make(view.findViewById(R.id.podcast_details),
                                getString(if (isSubscribed) R.string.action_subscribed else
                                    R.string.action_unsubscribed, podcast.title()), Snackbar.LENGTH_SHORT)

                        snackbar.setAction(getString(R.string.action_undo)) {
                            model.subscribe(podcast, podcastDetails)
                        }

                        snackbar.show()
                    })
                }

                model.episodes.observe(this, Observer { items ->
                    podcastView.setItems(items, object: PodcastEpisodeAdapter.EpisodeItemListener {
                        override fun download(item: PodcastEpisodeViewItem) {
                            model.download(item)
                        }

                        override fun favouriteClicked(item: PodcastEpisodeViewItem) {
                            model.favouriteEpisode(item)
                        }
                    })
                })

                model.description.observe(this, Observer { description ->
                    podcastView.setDescription(description)
                })

                model.webSite.observe(this, Observer { webSite ->
                    openWebSiteMenu?.setOnMenuItemClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webSite)))
                        false
                    }
                })

                model.episodesChanged.observe(this, Observer { changed ->
                    podcastView.notifyDataSetChanged(changed)
                })

                model.loadFeed(podcastDetails, podcast.id())

            })

            model.load(podcast)
        }

        return view
    }

    private fun syncSubscribeButton(subscribed: Boolean, manualyInvoked: Boolean = false) {
        if (!manualyInvoked) {
            val stateSet = intArrayOf(android.R.attr.state_checked * if (subscribed) 1 else -1)
            subscribe?.setImageState(stateSet, true)
        }
        subscribeMenu?.setTitle(if (subscribed) R.string.unsubscribe else R.string.subscribe)

        val stateSet = intArrayOf(android.R.attr.state_checked * if (subscribed) 1 else -1)
        subscribe?.setImageState(stateSet, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podcastView.fragmentCreated(podcast)
    }

    override fun getTitle() = ""

    override fun hasNavigation() = false

    override fun hasBackNavigation() = true

    companion object {

        private val ARG_PODCAST = "arg_podcast"

        val TAG = "PodcastDetailsFragment"

        fun newInstance(podcast: PodcastAdapterEntry): PodcastDetailsFragment {
            return PodcastDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PODCAST, podcast)
                }
            }
        }

    }

}