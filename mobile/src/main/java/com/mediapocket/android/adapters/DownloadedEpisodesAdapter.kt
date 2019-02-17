package com.mediapocket.android.adapters

import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.di.MainComponentLocator
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.playback.model.DownloadedEpisodeItem
import com.mediapocket.android.utils.FileUtils
import com.mediapocket.android.utils.TimeUtils
import com.tonyodev.fetch2.Download
import io.reactivex.functions.Consumer
import org.jetbrains.anko.*
import java.io.File
import java.lang.NullPointerException

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesAdapter(episodes: List<PodcastEpisodeItem> = arrayListOf()) : RecyclerView.Adapter<DownloadedEpisodesAdapter.EpisodeViewHolder>() {

    private val localEpisodes = ArrayList<PodcastEpisodeItem>(episodes)

    private val swipeLayoutHelper: ViewBinderHelper
    private val metaRetriever = MediaMetadataRetriever()

    @set:Inject
    lateinit var manager: PodcastDownloadManager

    init {
        MainComponentLocator.mainComponent.inject(this)

        manager.subscribeForDownloads(Consumer { download ->

            localEpisodes.find { it -> it.downloadId == download.downloadId }?.let {
                it.state = download.state
                notifyItemChanged(localEpisodes.indexOf(it), download)
            }
        })

        swipeLayoutHelper = ViewBinderHelper()
        swipeLayoutHelper.setOpenOnlyOne(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.downloaded_episode, parent, false))

    override fun getItemCount() = localEpisodes.size


    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) =
            holder.bind(position, swipeLayoutHelper, localEpisodes[position], null)

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int, payloads: List<Any>) =
            holder.bind(position, swipeLayoutHelper,localEpisodes[position], if (payloads.isEmpty()) null else payloads[0] as PodcastDownloadItem)

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootView = itemView.findViewById<View>(R.id.root_view)
        private val swipeLayout = itemView.findViewById<SwipeRevealLayout>(R.id.swipe_view)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val podcast = itemView.findViewById<TextView>(R.id.podcast_details)
        private val date = itemView.findViewById<TextView>(R.id.pub_date)
        private val image = itemView.findViewById<ImageView>(R.id.image)
        private val delete = itemView.findViewById<ImageView>(R.id.delete_episode)
        private val progressFrame = itemView.findViewById<View>(R.id.download_progress)
        private val progress= itemView.findViewById<CircularProgressBar>(R.id.download_progress_bar)
        private val progressPercents= itemView.findViewById<TextView>(R.id.download_progress_percents)
        private val duration= itemView.findViewById<TextView>(R.id.duration)
        private val size= itemView.findViewById<TextView>(R.id.size)

        fun bind(position: Int, swipeLayoutHelper: ViewBinderHelper, item: PodcastEpisodeItem, download: PodcastDownloadItem?) {
            title.text = item.title
            podcast.text = item.podcastTitle
            date.text = dateFormatter.format(Date(item.pubDate))

            var durationTime: Long = 0
//            item.length?.let {
//                durationTime = it
//            } ?: run {
                metaRetriever.setDataSource(item.localPath)
                durationTime = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
//            }

            duration.text = TimeUtils.millisToShortDHMS(durationTime)

//            itemView.invalidate()
            try {
                size.text = FileUtils.formatBytes(File(item.localPath).length())
            } catch (e: NullPointerException) {
                size.text = ""
            }

            delete.setOnClickListener{
                manager.delete(PodcastDownloadItem(item)).subscribe{
                    localEpisodes.remove(item)
                    notifyItemRemoved(position)
                }
            }

            swipeLayoutHelper.bind(swipeLayout, item.id)

            progressFrame.visibility = if (item.state == PodcastEpisodeItem.STATE_DOWNLOADED) View.GONE else View.VISIBLE

            Glide.with(itemView.context).load(item.imageUrl).into(image)

            download?.let {
                progress.progress = download.progress.toFloat()
                progressPercents.text = (download.progress.toString() + "%")
            }

            rootView.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(DownloadedEpisodeItem(item.link))) }

        }
    }

    companion object {
        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }
}