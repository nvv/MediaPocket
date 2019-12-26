package com.mediapocket.android.adapters

import android.content.Context
import android.graphics.drawable.Animatable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ShareCompat
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.core.download.extensions.isDownloaded
import com.mediapocket.android.core.download.extensions.isError
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.model.Item
import com.mediapocket.android.playback.model.RssEpisodeItem
import com.mediapocket.android.utils.GlobalUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer


/**
 * @author Vlad Namashko
 */
class PodcastEpisodeAdapter(private val context: Context,
                            private val items: List<Item>?,
                            private val parentLink: String,
                            private val podcastId: String?,
                            private val subscription: CompositeDisposable,
                            private val manager: PodcastDownloadManager) : androidx.recyclerview.widget.RecyclerView.Adapter<PodcastEpisodeAdapter.PodcastItemViewHolder>() {

    private val data = mutableListOf<PodcastEpisode>()
    private val dataMap = LinkedHashMap<String, PodcastEpisode>()
    private var accentColor: Int = -1

    private val mediaConnection : MediaSessionConnection
    private val callback: MediaControllerCompat.Callback
    private var lastActiveEpisode: PodcastEpisode? = null

//    @set:Inject
//    lateinit var manager: PodcastDownloadManager

    init {

//        MainComponentLocator.mainComponent.inject(this)

        items?.forEachIndexed { index, it ->
            val newItem = PodcastEpisode(index, it)
            if (!it.link.isNullOrEmpty()) {
                data.add(newItem)
                dataMap[PodcastEpisodeItem.convertLinkToId(it.link)] = newItem
            }
        }

        subscription.add(manager.subscribeForDownloads(Consumer { download ->
            val updateItem = dataMap[download.id]
            updateItem?.let {
                updateItem.download = download
                notifyItemChanged(updateItem.position, download)
            }
        }))

        subscription.add(manager.subscribeForDatabase(Consumer { records ->
            processItems(records)
            notifyDataSetChanged()
        }))

        mediaConnection = MediaSessionConnection.getInstance(context)

        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                mediaConnection.mediaController.metadata?.description?.mediaId?.let {
                    itemPlaybackChanged(it, state.isPlaying)
                }
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                metadata?.description?.mediaId?.let {
                    itemPlaybackChanged(it, mediaConnection.mediaController.playbackState.isPlaying)
                }
            }

            private fun itemPlaybackChanged(itemLink: String, isPlaying: Boolean): Unit? {
                val item = dataMap[PodcastEpisodeItem.convertLinkToId(itemLink)]
                return item?.let { episode ->
                    if (lastActiveEpisode != episode) {
                        lastActiveEpisode?.let {
                            it.isPlaying = null
                            notifyItemChanged(it.position)
                        }
                    }
                    lastActiveEpisode = episode

                    item.isPlaying = isPlaying
                    notifyItemChanged(episode.position)
                }
            }

        }

        mediaConnection.mediaController.metadata?.let {
            it.description?.mediaId?.let {
                val item = dataMap[PodcastEpisodeItem.convertLinkToId(it)]
                item?.let { episode ->
                    lastActiveEpisode = episode

                    item.isPlaying = mediaConnection.mediaController.playbackState.isPlaying
                    notifyItemChanged(episode.position)
                }
            }
        }

        mediaConnection.registerMediaControllerCallback(callback)
    }

    private fun processItems(records: List<PodcastDownloadItem>) {
        data.forEach {
            it.download = null
        }

        records.forEach { download ->
            val item = dataMap[download.id]
            item?.let {
                item.download = download
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        mediaConnection.unregisterMediaControllerCallback(callback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder {
        return PodcastItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_episode_item, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun setColors(accentColor: Int) {
        this.accentColor = accentColor
        notifyDataSetChanged()
    }

    inner class PodcastItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

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

        fun bind(item: PodcastEpisode) {
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

            itemView.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(RssEpisodeItem(item.item.link, parentLink))) }


//            item.download?.let { download ->
//                delete.setOnClickListener {
//                    subscription.add(manager.delete(download).subscribe())
//                }
//            }

            status.setOnClickListener {
                clickDownload(item, manager)
            }

            share.setOnClickListener {
                ShareCompat.IntentBuilder.from(GlobalUtils.getActivity(context))
                        .setText(item.link)
                        .setSubject(item.title)
                        .setType("text/plain")
                        .setChooserTitle(R.string.sharing)
                        .startChooser()
            }

            error.setOnClickListener {
                clickDownload(item, manager)
            }

            favourite.setOnClickListener {
                subscription.add(manager.favourite(podcastId, item.item).subscribe { items ->
                    processItems(items)
                    val updated = items.find {
                        it.id == PodcastEpisodeItem.convertLinkToId(item.link)
                    }
                    updated?.let {
                        val stateSet = intArrayOf(android.R.attr.state_checked * if (updated.favourite) 1 else -1)
                        favourite.post {
                            favourite.setImageState(stateSet, true)
                        }
                    }
                })
            }

            val download = item.download != null && item.download?.favourite!!
            val stateSet = intArrayOf(android.R.attr.state_checked * if (download) 1 else -1)
            favourite.setImageState(stateSet, true)


            playback.visibility = if (item.isPlaying != null && item.isPlaying!!) View.VISIBLE else View.GONE
            item.isPlaying?.let { isPlaying ->

                if (!(playback.drawable as Animatable).isRunning) {
                    (playback.drawable as Animatable).start()
                }
            }

            error.visibility = if (item.download?.state == PodcastEpisodeItem.STATE_ERROR) View.VISIBLE else View.GONE

//            (status.parent as ViewGroup).visibility = if (item.download?.state == PodcastEpisodeItem.STATE_DOWNLOADED) View.GONE else View.VISIBLE
            status.setImageResource(when (item.download?.state) {
                PodcastEpisodeItem.STATE_DOWNLOADED -> R.drawable.ic_downloaded
                PodcastEpisodeItem.STATE_PAUSED -> R.drawable.ic_play
                PodcastEpisodeItem.STATE_DOWNLOADING, PodcastEpisodeItem.STATE_ADDED, PodcastEpisodeItem.STATE_WAITING_FOR_NETWORK -> R.drawable.ic_pause
                else -> R.drawable.ic_download
            })

            progress.visibility = if (item.download != null && (item.download?.state == PodcastEpisodeItem.STATE_ADDED || item.download?.state == PodcastEpisodeItem.STATE_DOWNLOADING)) View.VISIBLE else View.GONE
//            delete.visibility = if (item.download?.state == PodcastEpisodeItem.STATE_DOWNLOADED) View.VISIBLE else View.GONE

            item.download?.let {
                progress.progress = it.progress.toFloat()
            }

        }

        private fun clickDownload(item: PodcastEpisode, manager: PodcastDownloadManager) {
            if (item.download != null && item.download?.state != PodcastEpisodeItem.STATE_NONE) {
                item.download?.let {
                    if (it.isDownloaded) {

                    } else if (!it.isError) {
                        manager.pause(it.downloadId)
                    } else {
                        subscription.add(manager.download(podcastId, item.item)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe())
                    }
                }
            } else {
                subscription.add(manager.download(podcastId, item.item)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe())
            }
        }
    }

    inner class PodcastEpisode(val position: Int, val item: Item) {

        val title = item.title

        val description = item.description

        val pubDate = item.dateFormatted()

        val link = item.link

        var download: PodcastDownloadItem? = null

        var isPlaying: Boolean? = null
    }

}
