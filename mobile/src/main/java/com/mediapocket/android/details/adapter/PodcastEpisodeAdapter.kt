package com.mediapocket.android.details.adapter

import android.graphics.drawable.Animatable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.view.EpisodeItemListener
import com.mediapocket.android.view.PodcastEpisodeToolbar
import com.mediapocket.android.details.viewitem.*
import com.mediapocket.android.playback.model.RssEpisodeItem


/**
 * @author Vlad Namashko
 */
class PodcastEpisodeAdapter(
        private val items: List<PodcastEpisodeViewItem>,
        private val listener: EpisodeItemListener? = null
) : RecyclerView.Adapter<PodcastEpisodeAdapter.PodcastItemViewHolder>() {

    private var accentColor: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder =
            PodcastItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_episode_item, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) =
            holder.bind(items[position])

    fun setColors(accentColor: Int) {
        this.accentColor = accentColor
        notifyDataSetChanged()
    }

    inner class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val pubDate = itemView.findViewById<TextView>(R.id.pubDate)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val description = itemView.findViewById<TextView>(R.id.description)
        private val playback = itemView.findViewById<ImageView>(R.id.episodePlaybackStatus)
        private val toolbar = itemView.findViewById<PodcastEpisodeToolbar>(R.id.toolbar)

        fun bind(item: PodcastEpisodeViewItem) {
            title.text = item.title
            description.text = Html.fromHtml(item.description)
            pubDate.text = item.puDateFormatted

            if (accentColor != -1) {
                playback.setColorFilter(accentColor)
                toolbar.applyTint(accentColor)
            }

            itemView.setOnClickListener {
                RxBus.default.postEvent(PlayPodcastEvent(RssEpisodeItem(item.link, item.rssLink)))
            }

            playback.visibility = if (item.isPlaying) View.VISIBLE else View.GONE

            item.isPlaying.let {
                if (!(playback.drawable as Animatable).isRunning) {
                    (playback.drawable as Animatable).start()
                }
            }

            toolbar.bind(item, listener)
        }
    }


}
