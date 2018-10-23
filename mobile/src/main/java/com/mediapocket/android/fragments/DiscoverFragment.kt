package com.mediapocket.android.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.*
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadGenreItemsEvent
import com.mediapocket.android.events.OpenSearchEvent
import com.mediapocket.android.model.DiscoverData
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.view.GenreListView
import com.mediapocket.android.view.NetworkListView
import com.mediapocket.android.view.PodcastListView
import com.mediapocket.android.viewmodels.PodcastViewModel
import io.reactivex.disposables.CompositeDisposable


/**
 * @author Vlad Namashko
 */
class DiscoverFragment : BaseFragment() {

    private lateinit var model: PodcastViewModel

    //    protected val adapter: PodcastListAdapter = PodcastListAdapter()
    protected val subscription: CompositeDisposable = CompositeDisposable()

    protected var loading: ProgressBar? = null
    protected var podcastList: LinearLayout? = null
    protected var podcastFrame: View? = null
//    protected var podcasts: RecyclerView? = null

    //    private var items: List<PodcastDiscoverResult>? = null
//    private var items: Map<String, PodcastDiscoverResult>? = null
    private var discoverData: DiscoverData? = null
    private var podcastListInitialized = false

    private val podcastRecyclers = mutableListOf<ItemListView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProviders.of(this).get(PodcastViewModel::class.java)

        subscription.add(
                model.getDiscoverData(getString(R.string.default_podcasts).split(",")).subscribe { data: DiscoverData ->
                    discoverData = data
                    if (view != null) {
                        initPodcasts()
                    }
                })
    }

    private fun initPodcasts() {
        podcastRecyclers.clear()

        discoverData?.let {

            var i = 0
            it.podcastData.forEach { res ->

                val list = PodcastListView(podcastList?.context, i)
                res.key.toIntOrNull()?.let {
                    list.addMoreAction { RxBus.default.postEvent(LoadGenreItemsEvent(res.key.toInt())) }
                }

                podcastList?.addView(list)
                list.load(res.value.title(), PodcastAdapterEntry.convertToAdapterEntries(res.value), i++)

                podcastRecyclers.add(list)
            }

            val genres = GenreListView(podcastList?.context, i++)
            genres.load(it.genres.genres.values.toList())
            podcastList?.addView(genres)
            podcastRecyclers.add(genres)

            val networks = NetworkListView(podcastList?.context, i++)
            networks.load(it.networks.networks)
            podcastList?.addView(networks)
            podcastRecyclers.add(networks)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.discover_podcast, container, false)

        podcastList = view?.findViewById(R.id.podcasts)
        podcastFrame = view?.findViewById(R.id.discover_list)
        loading = view?.findViewById(R.id.loading)

        subscription.add(model.loading().subscribe { isLoading -> syncVisivility(isLoading) })

        initPodcasts()


        podcastRecyclers.forEachIndexed { index, podcastListView ->
            arguments?.getParcelable<Parcelable>("POS$index")?.let {
//                podcastListView.post {
//                    podcastListView.restoreScrollPosition(it)
//                }
            }
        }


        setHasOptionsMenu(true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.let {
            if (!it.isFinishing) {
                if (arguments == null) {
                    arguments = Bundle()
                }

                podcastRecyclers.forEachIndexed { index, podcastListView ->
                    arguments?.putParcelable("POS$index", podcastListView.getScrollPosition())
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)

        val search: SearchView = menu.findItem(R.id.action_search).actionView as SearchView

        search.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                RxBus.default.postEvent(OpenSearchEvent())

//                podcasts?.let {
//                    if (arguments == null) {
//                        arguments = Bundle()
//                    }
//
//                    arguments?.putInt("POS", it.computeVerticalScrollOffset())
//                }
            }
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun syncVisivility(isLoading: Boolean) {
        podcastFrame?.visibility = if (isLoading) View.GONE else View.VISIBLE
        loading?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        subscription.dispose()
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.title_discover)

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false

    companion object {
        fun newInstance(): DiscoverFragment {
            return DiscoverFragment()
        }

        val TAG = "DiscoverFragment"
    }

}