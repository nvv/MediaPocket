package com.mediapocket.android.adapters

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PodcastSelectedEvent
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.PodcastListView

/**
 * @author Vlad Namashko
 */
class PodcastListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

    private var items: List<PodcastAdapterEntry>? = null
    private var positionOnPage: Int = 0
    private var hasMore = false
    private var action: (() -> Unit)? = null

    fun load(items: List<PodcastAdapterEntry>, positionOnPage: Int) {
        this.items = items
        this.positionOnPage = positionOnPage
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.let {
            holder.bind(if (position < it.size) it[position] else action!!, position, positionOnPage)
        }
    }

    override fun getItemCount(): Int {
        var extra = if (hasMore) 1 else 0
        return extra + (items?.size ?: 0)
    }

    fun addMoreAction(action: () -> Unit) {
        hasMore = true
        this.action = action
    }

    override fun getItemViewType(position: Int): Int {
        return if (!hasMore) VIEW_TYPE_ITEM else (if (position == items?.size) VIEW_TYPE_MORE else VIEW_TYPE_ITEM)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            VIEW_TYPE_MORE -> MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_list_view_more, parent, false))
            else -> PodcastViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_list_item, parent, false))
        }
    }

    abstract class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        abstract fun bind(data: Any, position: Int, positionOnPage: Int)

        protected fun getSize() = (ViewUtils.getRealScreenSize(itemView.context).x / 2.6 - 2 * ITEM_GAP).toInt()
    }

    class MoreViewHolder(itemView: View) : ViewHolder(itemView) {

        override fun bind(data: Any, position: Int, positionOnPage: Int) {
            val size = getSize()
            itemView.layoutParams.width = size
            itemView.layoutParams.height = size

            itemView.setOnClickListener { (data as (() -> Unit)).invoke() }
        }
    }

    class PodcastViewHolder(itemView: View) : ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val image = itemView.findViewById<ImageView>(R.id.image)

        override fun bind(data: Any, position: Int, positionOnPage: Int) {
            val podcast = data as PodcastAdapterEntry
            title.text = podcast.title()
            val size = getSize()
            image.layoutParams.width = size
            image.layoutParams.height = size

            itemView.layoutParams.width = size

            Glide.with(image.context).load(podcast.logo()).into(image)

            ViewCompat.setTransitionName(image, position.toString() + "_" + positionOnPage + "_image")
            itemView.setOnClickListener { _ -> RxBus.default.postEvent(PodcastSelectedEvent(podcast, image))}
        }
    }

    companion object {
        val ITEM_GAP = ViewUtils.getDimensionSize(4).toInt()

        val VIEW_TYPE_ITEM = 0
        val VIEW_TYPE_MORE = 1
    }

}