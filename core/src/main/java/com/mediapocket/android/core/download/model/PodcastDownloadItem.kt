package com.mediapocket.android.core.download.model

/**
 * @author Vlad Namashko
 */
data class PodcastDownloadItem(
        val id: String,
        var state: Int,
        var progress: Int,
        var isDownloaded: Boolean,
        val title: String?,
        val downloadId: Int,
        val podcastId: String)