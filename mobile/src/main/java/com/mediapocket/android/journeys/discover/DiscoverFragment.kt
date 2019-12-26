package com.mediapocket.android.journeys.discover

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.widget.*
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadGenreItemsEvent
import com.mediapocket.android.events.OpenSearchEvent
import com.mediapocket.android.fragments.BaseFragment
import com.mediapocket.android.journeys.discover.adapter.PodcastDiscoverAdapter
import com.mediapocket.android.model.DiscoverData
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.view.GenreListView
import com.mediapocket.android.view.NetworkListView
import com.mediapocket.android.view.PodcastListView
import com.mediapocket.android.viewmodels.PodcastViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.discover_podcast.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * @author Vlad Namashko
 */
class DiscoverFragment : BaseFragment() {

    private lateinit var model: PodcastViewModel

    private var discoverData: DiscoverData? = null

    // special case - need to access this ASAP before fragment's view is created
    private lateinit var podcastList: RecyclerView

    private lateinit var adapter: PodcastDiscoverAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel::class.java)

        model.discoverData()
    }

    private fun initPodcasts() {
        podcastList.layoutManager?.onRestoreInstanceState(arguments?.getParcelable<Parcelable>(MAIN_RECYCLER_POSITION))
        discoverData?.let {

            // restore view state
            val states = mutableMapOf<Int, Parcelable?>()
            for (index in 0 .. (it.podcastData.values.size + 2)) {
                arguments?.getParcelable<Parcelable>("$RECYCLER_POSITION$index")?.let {
                    states[index] = it
                }
            }

            adapter = PodcastDiscoverAdapter(requireActivity(), it, states)
            podcastList.adapter = adapter
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.discover_podcast, container, false)
        podcastList = view.findViewById(R.id.podcastList)

        model.isLoading.observe(this, Observer {
            syncVisibility(it)
        })

        model.getDiscoverData.observe(this, Observer { data ->
            discoverData = data
            initPodcasts()
        })

        setHasOptionsMenu(true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // save view state
        activity?.let {
            if (!it.isFinishing) {
                if (arguments == null) {
                    arguments = Bundle()
                }

                arguments?.putParcelable(MAIN_RECYCLER_POSITION, podcastList.layoutManager?.onSaveInstanceState())

                adapter.onDestroyed()
                adapter.states.entries.forEach { item ->
                    arguments?.putParcelable("$RECYCLER_POSITION${item.key}", item.value)
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
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun syncVisibility(isLoading: Boolean) {
        podcastList.visibility = if (isLoading) View.GONE else View.VISIBLE
        loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.title_discover)

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false

    companion object {

        private const val MAIN_RECYCLER_POSITION = "MAIN_POS"
        private const val RECYCLER_POSITION = "POS"

        val TAG = "DiscoverFragment"

        fun newInstance(): DiscoverFragment {
            return DiscoverFragment()
        }
    }




}