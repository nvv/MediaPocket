package com.mediapocket.android.core.download.extensions

import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.dao.model.DownloadedPodcastItem

/**
 * @author Vlad Namashko
 */
inline val PodcastDownloadItem.isDownloaded
    get() = (state == DownloadedPodcastItem.STATE_DOWNLOADED)