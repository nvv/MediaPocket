package com.mediapocket.android.adapters

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.events.DeletePodcastEpisodeEvent
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.playback.model.DownloadedEpisodeItem
import com.mediapocket.android.utils.FileUtils
import com.mediapocket.android.utils.TimeUtils
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.downloaded_episode.view.*
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesAdapter(episodes: List<PodcastEpisodeItem> = arrayListOf()) : androidx.recyclerview.widget.RecyclerView.Adapter<DownloadedEpisodesAdapter.EpisodeViewHolder>() {

    private val localEpisodes = ArrayList<PodcastEpisodeItem>(episodes)

    private val swipeLayoutHelper: ViewBinderHelper
    private val metaRetriever = MediaMetadataRetriever()

//    @set:Inject
    lateinit var manager: PodcastDownloadManager

    init {
//        MainComponentLocator.mainComponent.inject(this)

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

    fun onItemRemoved(item: PodcastEpisodeItem, position: Int) {
        localEpisodes.remove(item)
        notifyItemRemoved(position)
    }

    inner class EpisodeViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, swipeLayoutHelper: ViewBinderHelper, item: PodcastEpisodeItem, download: PodcastDownloadItem?) {
            itemView.title.text = item.title
            itemView.podcast_details.text = item.podcastTitle
            itemView.pub_date.text = dateFormatter.format(Date(item.pubDate))

            var durationTime: Long = 0
            try {
                metaRetriever.setDataSource(item.localPath)
                durationTime = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
            } catch (e: Exception) {
                item.length?.let {
                    durationTime = it
                }
            }

            itemView.duration.text = TimeUtils.millisToShortDHMS(durationTime)

            item.localPath?.let {
                itemView.size.text = FileUtils.formatBytes(File(it).length())
            }

            itemView.delete_episode_frame.setOnClickListener{
                RxBus.default.postEvent(DeletePodcastEpisodeEvent(item, position))
            }

            swipeLayoutHelper.bind(itemView.swipe_view, item.id)

            itemView.download_progress.visibility = if (item.state == PodcastEpisodeItem.STATE_DOWNLOADED) View.GONE else View.VISIBLE

            Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .apply(RequestOptions().placeholder(R.drawable.ic_musical_note).fitCenter())
                    .into(itemView.image)

            download?.let {
                itemView.download_progress_bar.progress = download.progress.toFloat()
                itemView.download_progress_percents.text = (download.progress.toString() + "%")
            }

            itemView.root_view.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(DownloadedEpisodeItem(item.getMediaPath()))) }

        }
    }

    companion object {
        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }
}