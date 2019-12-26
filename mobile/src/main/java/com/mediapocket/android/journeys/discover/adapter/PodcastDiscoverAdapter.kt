package com.mediapocket.android.journeys.discover.adapter

import android.content.Context
import android.os.Parcelable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mediapocket.android.ItemListView
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadGenreItemsEvent
import com.mediapocket.android.model.DiscoverData
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.view.GenreListView
import com.mediapocket.android.view.NetworkListView
import com.mediapocket.android.view.PodcastListView

class PodcastDiscoverAdapter(
        val context: Context,
        val data: DiscoverData,
        val states: MutableMap<Int, Parcelable?>
) : RecyclerView.Adapter<PodcastDiscoverAdapter.DiscoverItemHolder>() {

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

    abstract inner class DiscoverItemHolder(val view: ItemListView) : RecyclerView.ViewHolder(view)

    inner class ListViewHolder(val list: PodcastListView) : DiscoverItemHolder(list)

    inner class GenreViewHolder(val list: GenreListView) : DiscoverItemHolder(list)

    inner class NetworkViewHolder(val list: NetworkListView) : DiscoverItemHolder(list)

    companion object {
        const val TYPE_PODCAST_LIST = 0
        const val TYPE_GENRE = 1
        const val TYPE_NETWORK = 2
    }
}