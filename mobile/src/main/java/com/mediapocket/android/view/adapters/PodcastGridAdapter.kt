package com.mediapocket.android.view.adapters

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

/**
 * @author Vlad Namashko
 */
class PodcastGridAdapter : RecyclerView.Adapter<PodcastGridAdapter.PodcastViewHolder>() {

    private var items: List<PodcastAdapterEntry>? = null
    private var itemsInRow: Int = 2

    fun setItems(items: List<PodcastAdapterEntry>) {
        this.items = items
    }

    fun setItemsInRowCount(itemsInRow: Int) {
        this.itemsInRow = itemsInRow
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        items?.let { holder.bind(it[position], itemsInRow) }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        return PodcastViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_item, parent, false))
    }


    class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val image = itemView.findViewById<ImageView>(R.id.image)

        fun bind(podcast: PodcastAdapterEntry, itemsInRow: Int) {
            title.text = podcast.title()
            image.layoutParams.height = ViewUtils.getRealScreenSize(itemView.context).x / itemsInRow - 2 * ITEM_GAP


            Glide.with(image.context).load(podcast.logo()).into(image)
            ViewCompat.setTransitionName(image, position.toString() + "_image")
            itemView.setOnClickListener({ _ -> RxBus.default.postEvent(PodcastSelectedEvent(podcast, image))})
        }
    }

    companion object {
        val ITEM_GAP = ViewUtils.getDimensionSize(4).toInt()
    }

}