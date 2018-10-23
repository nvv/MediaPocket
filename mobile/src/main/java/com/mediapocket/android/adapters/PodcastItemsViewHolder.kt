package com.mediapocket.android.adapters

/**
 * @author Vlad Namashko
 */

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.model.Item

/**
 * @author Vlad Namashko
 */
class PodcastItemsAdapter(val items: List<Item>?, val parentLink: String) : RecyclerView.Adapter<PodcastItemsAdapter.PodcastItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder {
        return PodcastItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_details_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) {
        items?.let { holder.bind(it[position]) }
    }

    inner class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val pubDate = itemView.findViewById<TextView>(R.id.pub_date)
        private val title = itemView.findViewById<TextView>(R.id.title)

        fun bind(item: Item) {
            title.text = item.title
            pubDate.text = item.dateFormatted()

            itemView.setOnClickListener({ RxBus.default.postEvent(PlayPodcastEvent(item, parentLink)) })
        }
    }

}
