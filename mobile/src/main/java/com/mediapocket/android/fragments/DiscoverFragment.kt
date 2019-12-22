package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.widget.*
import android.view.*
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

    protected val subscription: CompositeDisposable = CompositeDisposable()

    protected var loading: ProgressBar? = null
    protected var podcastList: androidx.recyclerview.widget.RecyclerView? = null
    private var discoverData: DiscoverData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProviders.of(this, viewModelFactory).get(PodcastViewModel::class.java)

        subscription.add(
                model.getDiscoverData().subscribe { data: DiscoverData ->
                    discoverData = data
                    if (view != null) {
                        initPodcasts()
                    }
                })
    }

    private fun initPodcasts() {
        podcastList?.layoutManager?.onRestoreInstanceState(arguments?.getParcelable<Parcelable>("MAIN_POS"))
        discoverData?.let {

            val states = mutableMapOf<Int, Parcelable?>()
            for (index in 0 .. (it.podcastData.values.size + 2)) {
                arguments?.getParcelable<Parcelable>("POS$index")?.let {
                    states[index] = it
                }
            }


            podcastList?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(podcastList?.context)
            podcastList?.adapter = Adapter(requireActivity(), it, states)

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.discover_podcast, container, false)

        podcastList = view?.findViewById(R.id.podcasts)
        loading = view?.findViewById(R.id.loading)

        subscription.add(model.loading().subscribe { isLoading -> syncVisivility(isLoading) })

        initPodcasts()

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

                arguments?.putParcelable("MAIN_POS", podcastList?.layoutManager?.onSaveInstanceState())

                val adapter = podcastList?.adapter as Adapter
                adapter.onDestroyed()
                adapter.states.entries.forEach { item ->
                    arguments?.putParcelable("POS${item.key}", item.value)
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

    private fun syncVisivility(isLoading: Boolean) {
        podcastList?.visibility = if (isLoading) View.GONE else View.VISIBLE
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

    class Adapter(val context: Context, private val data: DiscoverData, val states: MutableMap<Int, Parcelable?>) :
            androidx.recyclerview.widget.RecyclerView.Adapter<Adapter.DiscoverItemHolder>() {

        val keys = data.podcastData.keys.toList()
        val items = data.podcastData.values.toList()

        private val boundViewHolders = mutableSetOf<DiscoverItemHolder>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverItemHolder {
            return when (viewType) {
                TYPE_GENRE -> GenreViewHolder(GenreListView(context))
                TYPE_NETWORK -> NetworkViewHolder(NetworkListView(context))
                else -> ListViewHolder(PodcastListView(context))
            }
        }

        override fun getItemCount() = items.size + 2

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                items.size -> TYPE_GENRE
                items.size + 1 -> TYPE_NETWORK
                else -> TYPE_PODCAST_LIST
            }
        }

        override fun onBindViewHolder(holder: DiscoverItemHolder, position: Int) {
            val state = states[position]

            boundViewHolders.add(holder)
            state?.let {
                holder.view.restoreInstanceState(it)
            } ?: run {
                holder.view.scrollToBeginning()
            }

            when (holder) {
                is GenreViewHolder -> holder.list.load(data.genres.genres.values.toList(), position)
                is NetworkViewHolder -> holder.list.load(data.networks.networks, position)
                is ListViewHolder -> {
                    val item = items[position]
                    holder.list.load(item.title(), PodcastAdapterEntry.convertToAdapterEntries(item), position)

                    keys[position].toIntOrNull()?.let {
                        holder.list.addMoreAction { RxBus.default.postEvent(LoadGenreItemsEvent(it)) }
                    }
                }
            }
        }

        fun onDestroyed() {
            boundViewHolders.forEach { holder ->
                states[holder.view.positionOnPage()] = holder.view.getInstanceState()
            }
        }

        override fun onViewRecycled(holder: DiscoverItemHolder) {
            boundViewHolders.remove(holder)
            states[holder.view.positionOnPage()] = holder.view.getInstanceState()
            super.onViewRecycled(holder)
        }

        abstract inner class DiscoverItemHolder(val view: ItemListView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

        inner class ListViewHolder(val list: PodcastListView) : DiscoverItemHolder(list)

        inner class GenreViewHolder(val list: GenreListView) : DiscoverItemHolder(list)

        inner class NetworkViewHolder(val list: NetworkListView) : DiscoverItemHolder(list)

        companion object {
            const val TYPE_PODCAST_LIST = 0
            const val TYPE_GENRE = 1
            const val TYPE_NETWORK = 2
        }
    }


}