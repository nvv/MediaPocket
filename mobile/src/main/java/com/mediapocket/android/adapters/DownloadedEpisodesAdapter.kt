package com.mediapocket.android.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mediapocket.android.R
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import org.jetbrains.anko.*

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesAdapter(var episodes: List<PodcastEpisodeItem> = arrayListOf()) : RecyclerView.Adapter<DownloadedEpisodesAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.downloaded_episode, parent, false))

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) = holder.bind(episodes[position])

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val podcast = itemView.findViewById<TextView>(R.id.podcast_details)
        private val date = itemView.findViewById<TextView>(R.id.pub_date)
        private val image = itemView.findViewById<ImageView>(R.id.image)

        fun bind(item: PodcastEpisodeItem) {
            title.text = item.title
            podcast.text = item.podcastTitle
            date.text = dateFormatter.format(Date(item.pubDate))

            Glide.with(itemView.context).load(item.imageUrl).into(image)
        }
    }

    companion object {
        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }
}