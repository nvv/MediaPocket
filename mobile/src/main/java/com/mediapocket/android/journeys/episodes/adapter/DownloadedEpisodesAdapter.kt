package com.mediapocket.android.journeys.episodes.adapter

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.journeys.details.viewitem.isDownloaded
import com.mediapocket.android.journeys.details.viewitem.isDownloading
import com.mediapocket.android.journeys.details.viewitem.isError
import com.mediapocket.android.playback.model.DownloadedEpisodeItem
import com.mediapocket.android.utils.FileUtils
import kotlinx.android.synthetic.main.downloaded_episode.view.*
import java.io.File

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesAdapter(
        private val episodes: List<PodcastEpisodeViewItem>,
        private val deleteAction: ((item: PodcastEpisodeViewItem) -> Unit)? = null
): RecyclerView.Adapter<DownloadedEpisodesAdapter.EpisodeViewHolder>() {

    private val swipeLayoutHelper: ViewBinderHelper = ViewBinderHelper()

    init {
        swipeLayoutHelper.setOpenOnlyOne(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.downloaded_episode, parent, false))

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) =
            holder.bind(swipeLayoutHelper, episodes[position])

    inner class EpisodeViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(swipeLayoutHelper: ViewBinderHelper, item: PodcastEpisodeViewItem) {
            itemView.title.text = item.title
            itemView.podcast_details.text = item.podcastTitle
            itemView.pub_date.text = item.puDateFormatted

            itemView.duration.text = item.durationFormatted

            item.localPath?.let {
                itemView.size.text = FileUtils.formatBytes(File(it).length())
            }

            itemView.delete_episode_frame.setOnClickListener{
                deleteAction?.invoke(item)
            }

            swipeLayoutHelper.bind(itemView.swipe_view, item.id)

            Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .apply(RequestOptions().placeholder(R.drawable.ic_musical_note).fitCenter())
                    .into(itemView.image)

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

            itemView.root_view.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(DownloadedEpisodeItem(item.getMediaPath()))) }

        }
    }

}