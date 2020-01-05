package com.mediapocket.android.core.download.extensions

import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */

inline val PodcastDownloadItem.isDownloading
    get() = (state == PodcastEpisodeItem.STATE_DOWNLOADING)

inline val PodcastDownloadItem.isDownloaded
    get() = (state == PodcastEpisodeItem.STATE_DOWNLOADED)

inline val PodcastDownloadItem.isError
    get() = (state == PodcastEpisodeItem.STATE_ERROR)