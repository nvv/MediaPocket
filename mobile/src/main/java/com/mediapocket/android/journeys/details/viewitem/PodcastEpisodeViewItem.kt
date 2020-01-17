package com.mediapocket.android.journeys.details.viewitem

import androidx.annotation.DrawableRes
import com.mediapocket.android.R
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item

class PodcastEpisodeViewItem(
        val position: Int,
        val item: Item,
        val rssLink: String,
        val podcastId: String?) {

    val id by lazy { PodcastEpisodeItem.convertLinkToId(item.link) }

    val title by lazy { item.title }

    val description by lazy { item.description }

    val pubDate by lazy { item.dateFormatted() }

    val link by lazy { item.link }

    var isPlaying: Boolean = false

    var isFavourite: Boolean = false

    var downloadState: DownloadState? = null

    @DrawableRes fun getStatusIcon(): Int = when {
        isDownloaded -> R.drawable.ic_downloaded
        isPaused -> R.drawable.ic_play
        downloadState != null -> R.drawable.ic_pause
        else -> R.drawable.ic_download
    }

}

data class DownloadState(
        var isDownloaded: Boolean = false,
        var progress: Int = 0,
        var state: Int = PodcastEpisodeItem.STATE_NONE,
        var error: String? = null
)

inline val PodcastEpisodeViewItem.isDownloading
    get() = (downloadState?.state == PodcastEpisodeItem.STATE_DOWNLOADING)

inline val PodcastEpisodeViewItem.isDownloaded
    get() = (downloadState?.isDownloaded ?: false)

inline val PodcastEpisodeViewItem.isPaused
    get() = (downloadState?.state == PodcastEpisodeItem.STATE_PAUSED)

inline val PodcastEpisodeViewItem.isError
    get() = (downloadState?.state == PodcastEpisodeItem.STATE_ERROR)
