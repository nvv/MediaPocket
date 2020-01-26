package com.mediapocket.android.journeys.episodes.adapter

import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.playback.model.DownloadedEpisodeItem
import com.mediapocket.android.utils.FileUtils
import kotlinx.android.synthetic.main.downloaded_episode.view.*
import java.io.File

/**
 * @author Vlad Namashko
 */
class LocalEpisodesAdapter(
        private val episodes: List<PodcastEpisodeViewItem>,
        private val deleteAction: ((item: PodcastEpisodeViewItem) -> Unit)? = null
): RecyclerView.Adapter<LocalEpisodesAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.downloaded_episode, parent, false))

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) =
            holder.bind(episodes[position])

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: PodcastEpisodeViewItem) {
            itemView.title.text = item.title
            itemView.podcastDetails.text = item.podcastTitle
            itemView.pubDate.text = item.puDateFormatted

            itemView.duration.text = item.durationFormatted

            item.localPath?.let {
                itemView.size.text = FileUtils.formatBytes(File(it).length())
            }

            Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .apply(RequestOptions().placeholder(R.drawable.ic_musical_note).centerCrop())
                    .into(itemView.image)

            /*
            if (item.isDownloading) {
                val progress = item.downloadState?.progress
                itemView.download_progress_bar.progress = progress?.toFloat() ?: 0f
                itemView.download_progress_percents.text = (progress.toString() + "%")

                itemView.download_progress_bar.visibility = View.VISIBLE
                itemView.download_progress_percents.visibility = View.VISIBLE
            } else {
                itemView.download_progress_bar.visibility = View.GONE
                itemView.download_progress_percents.visibility = View.GONE
            }
            */



            itemView.episodePlaybackStatus.visibility = if (item.isPlaying) View.VISIBLE else View.GONE

            item.isPlaying.let {
                if (!(itemView.episodePlaybackStatus.drawable as Animatable).isRunning) {
                    (itemView.episodePlaybackStatus.drawable as Animatable).start()
                }
            }


            itemView.root_view.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(DownloadedEpisodeItem(item.getMediaPath()))) }

        }
    }

}