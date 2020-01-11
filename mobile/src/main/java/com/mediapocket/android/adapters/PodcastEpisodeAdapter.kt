package com.mediapocket.android.adapters

import android.content.Context
import android.graphics.drawable.Animatable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.journeys.details.viewitem.*
import com.mediapocket.android.playback.model.RssEpisodeItem
import com.mediapocket.android.utils.GlobalUtils


/**
 * @author Vlad Namashko
 */
class PodcastEpisodeAdapter(
        private val items: List<PodcastEpisodeViewItem>,
        private val listener: EpisodeItemListener? = null
) : RecyclerView.Adapter<PodcastEpisodeAdapter.PodcastItemViewHolder>() {

    private var accentColor: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder {
        return PodcastItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_episode_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun setColors(accentColor: Int) {
        this.accentColor = accentColor
        notifyDataSetChanged()
    }

    inner class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val pubDate = itemView.findViewById<TextView>(R.id.pubDate)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val description = itemView.findViewById<TextView>(R.id.description)
        private val error = itemView.findViewById<TextView>(R.id.error)
        private val status = itemView.findViewById<ImageView>(R.id.downloadStatus)
        private val progress = itemView.findViewById<CircularProgressBar>(R.id.downloadProgress)
//        private val delete = itemView.findViewById<ImageView>(R.id.delete_episode)
        private val playback = itemView.findViewById<ImageView>(R.id.episodePlaybackStatus)
        private val favourite = itemView.findViewById<ImageView>(R.id.episodeFavorite)
        private val share = itemView.findViewById<ImageView>(R.id.episodeShare)
        private val more = itemView.findViewById<ImageView>(R.id.episodeContextMenu)

        fun bind(item: PodcastEpisodeViewItem) {
            title.text = item.title
            description.text = Html.fromHtml(item.description)
            pubDate.text = item.pubDate

            if (accentColor != -1) {
                status.setColorFilter(accentColor)
                progress.foregroundStrokeColor = accentColor
//                delete.setColorFilter(accentColor)
                favourite.setColorFilter(accentColor)
                share.setColorFilter(accentColor)
                more.setColorFilter(accentColor)
                playback.setColorFilter(accentColor)
            }

            itemView.setOnClickListener {
                RxBus.default.postEvent(PlayPodcastEvent(RssEpisodeItem(item.link, item.rssLink)))
            }

            status.setOnClickListener {
                listener?.downloadClicked(item)
            }

            share.setOnClickListener {
//                ShareCompat.IntentBuilder.from(GlobalUtils.getActivity(context))
//                        .setText(item.link)
//                        .setSubject(item.title)
//                        .setType("text/plain")
//                        .setChooserTitle(R.string.sharing)
//                        .startChooser()
            }

            error.setOnClickListener {
                listener?.downloadClicked(item)
            }

            favourite.setOnClickListener {
                listener?.favouriteClicked(item)
            }

            val stateSet = intArrayOf(android.R.attr.state_checked * if (item.isFavourite) 1 else -1)
            favourite.setImageState(stateSet, true)

            playback.visibility = if (item.isPlaying) View.VISIBLE else View.GONE

            item.isPlaying.let {
                if (!(playback.drawable as Animatable).isRunning) {
                    (playback.drawable as Animatable).start()
                }
            }

            if (item.isError) {
                error.visibility = View.VISIBLE
                error.text = item.downloadState?.error
                status.setImageResource(R.drawable.ic_download)
            } else {
                error.visibility = View.GONE
                when {
                    item.isDownloaded -> status.setImageResource(R.drawable.ic_downloaded)
                    item.isPaused -> status.setImageResource(R.drawable.ic_play)
                    item.downloadState != null -> status.setImageResource(R.drawable.ic_pause)
                    else -> status.setImageResource(R.drawable.ic_download)
                }
            }

            if (item.isDownloading) {
                progress.visibility = View.VISIBLE
                progress.progress = item.downloadState?.progress?.toFloat() ?: 0F
            } else {
                progress.visibility = View.GONE
            }

        }
    }

    interface EpisodeItemListener {

        fun favouriteClicked(item: PodcastEpisodeViewItem)

        fun downloadClicked(item: PodcastEpisodeViewItem)
    }

}
