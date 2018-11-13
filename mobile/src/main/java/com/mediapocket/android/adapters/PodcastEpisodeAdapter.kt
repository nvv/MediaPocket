package com.mediapocket.android.adapters

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.DownloadedPodcastItem
import com.mediapocket.android.events.PlayPodcastEvent
import com.mediapocket.android.model.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView

/**
 * @author Vlad Namashko
 */
class PodcastEpisodeAdapter(val items: List<Item>?,
                            val parentLink: String,
                            private val podcastId: String?) : RecyclerView.Adapter<PodcastEpisodeAdapter.PodcastItemViewHolder>() {

    private val data = mutableListOf<PodcastEpisode>()
    private val dataMap = LinkedHashMap<String, PodcastEpisode>()
    private var accentColor: Int = -1
//    private var records: List<PodcastDownloadItem>? = null

    init {

        items?.forEachIndexed { index, it ->
            val newItem = PodcastEpisode(index, it)
            data.add(newItem)
            dataMap[DownloadedPodcastItem.convertLinkToId(it.link)] = newItem
        }

        val manager = DependencyLocator.getInstance().podcastDownloadManager
        manager.subscribeForDownloads(Consumer { download ->
            val updateItem = dataMap[download.id]
            updateItem?.let {
                updateItem.download = download
                notifyItemChanged(updateItem.position)
            }
        })

        manager.subscribeForDatabase(Consumer { records ->
            data.forEach {
                it.download = null
            }

            records.forEach { download ->
                val item = dataMap[download.id]
                item?.let {
                    item.download = download
                }
            }

            notifyDataSetChanged()
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastItemViewHolder {
        return PodcastItemViewHolder(ItemView().createView(AnkoContext.create(parent.context, parent)))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onBindViewHolder(holder: PodcastItemViewHolder, position: Int, payloads: List<Any>) {
        holder.bind(data[position], payloads)
    }

    class ItemView : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            return with(ui) {
                linearLayout {
                    setPadding(dip(16), dip(16), dip(16), dip(16))
                    backgroundResource = R.color.white_semi_transparent
                    lparams(width = matchParent, height = wrapContent)
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.LEFT

                    textView {
                        id = R.id.pub_date
                        ellipsize = TextUtils.TruncateAt.END
                        gravity = Gravity.CENTER
                        maxLines = 2
                        textColorResource = R.color.grey
                    }.lparams(width = wrapContent, height = wrapContent) {
                        rightMargin = dip(16)
                    }

                    textView {
                        id = R.id.title
                        gravity = Gravity.CENTER_VERTICAL
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 1
                        textSize = 15f
                        textColorResource = R.color.black
                    }.lparams(width = wrapContent, height = wrapContent) {
                        rightMargin = dip(16)
                        gravity = Gravity.CENTER_VERTICAL
                        weight = 1f
                    }

                    frameLayout {

                        customView<CircularProgressBar> {
                            id = R.id.download_progress
                            visibility = View.GONE
                        }.lparams(width = matchParent, height = matchParent) {
                            gravity = Gravity.CENTER
                        }

                        imageView {
                            id = R.id.download_status
                            imageResource = R.drawable.ic_download
                        }.lparams(width = dip(28), height = dip(28)) {
                            gravity = Gravity.CENTER
                        }

                    }.lparams(width = dip(32), height = dip(32)) {
                        rightMargin = dip(16)
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    imageView {
                        id = R.id.delete_episode
                        imageResource = R.drawable.ic_delete
                    }.lparams(width = dip(28), height = dip(28)) {
                        gravity = Gravity.CENTER
                    }

                }
            }
        }

    }

    fun setColors(accentColor: Int) {
        this.accentColor = accentColor
        notifyDataSetChanged()
    }

    inner class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val pubDate = itemView.findViewById<TextView>(R.id.pub_date)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val status = itemView.findViewById<ImageView>(R.id.download_status)
        private val progress = itemView.findViewById<CircularProgressBar>(R.id.download_progress)
        private val delete = itemView.findViewById<ImageView>(R.id.delete_episode)

        fun bind(item: PodcastEpisode, payloads: List<Any>? = null) {
            title.text = item.title
            pubDate.text = item.pubDate

            if (accentColor != -1) {
                status.setColorFilter(accentColor)
                progress.foregroundStrokeColor = accentColor
                delete.setColorFilter(accentColor)
            }

            itemView.setOnClickListener { RxBus.default.postEvent(PlayPodcastEvent(item.item, parentLink)) }

            val manager =  DependencyLocator.getInstance().podcastDownloadManager

            item.download?.let { download ->
                delete.setOnClickListener {
                    manager.delete(download).subscribe()
                }
            }

            status.setOnClickListener {
                manager.download(podcastId, item.item)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
            }

            status.setImageResource(when (item.download?.state) {
                DownloadedPodcastItem.STATE_DOWNLOADED -> R.drawable.ic_downloaded
                DownloadedPodcastItem.STATE_DOWNLOADING, DownloadedPodcastItem.STATE_ADDED -> R.drawable.ic_pause
                else -> R.drawable.ic_download
            })

            progress.visibility = if (item.download != null && (item.download?.state == DownloadedPodcastItem.STATE_ADDED || item.download?.state == DownloadedPodcastItem.STATE_DOWNLOADING)) View.VISIBLE else View.GONE
            delete.visibility = if (item.download?.state == DownloadedPodcastItem.STATE_DOWNLOADED) View.VISIBLE else View.GONE

            item.download?.let {
                progress.progress = it.progress.toFloat()
            }

        }
    }

    inner class PodcastEpisode(val position: Int, val item: Item) {

        val title = item.title

        val pubDate = item.dateFormatted()

        val link = item.link

        var download: PodcastDownloadItem? = null
    }

}
