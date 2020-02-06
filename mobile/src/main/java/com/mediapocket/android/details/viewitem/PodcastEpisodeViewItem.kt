package com.mediapocket.android.details.viewitem

import androidx.annotation.DrawableRes
import com.mediapocket.android.R
import com.mediapocket.android.dao.model.PodcastEpisodeItem

class PodcastEpisodeViewItem(
        val position: Int,
        val podcastTitle: String,
        val title: String,
        val description: String,
        val pubDate: Long,
        val puDateFormatted: String,
        val link: String?,
        val length: Long,
        val imageUrl: String,
        val rssLink: String,
        val podcastId: String?,
        val localPath: String?,
        val durationFormatted: String?) {

    val id by lazy { PodcastEpisodeItem.convertLinkToId(link) }

    var isPlaying: Boolean = false

    var isFavourite: Boolean = false

    var downloadState: DownloadState? = null

    @DrawableRes fun getStatusIcon(): Int = when {
        isDownloaded -> R.drawable.ic_downloaded
        isPaused -> R.drawable.ic_play
        downloadState != null -> R.drawable.ic_pause
        else -> R.drawable.ic_download
    }

    fun getMediaPath() = if (isDownloaded) localPath else link
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
