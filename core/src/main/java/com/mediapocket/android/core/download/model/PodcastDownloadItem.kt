package com.mediapocket.android.core.download.model

/**
 * @author Vlad Namashko
 */
data class PodcastDownloadItem (
        val id: String,
        var state: Int,
        var progress: Int,
        var isDownloaded: Boolean,
        val title: String?,
        val downloadId: Int) {

//    constructor(item: PodcastEpisodeItem) : this(item.id, item.state, 0, item.podcastId,
//                item.podcastTitle, item.title, item.description, item.link,
//                item.pubDate, item.length, item.favourite, item.imageUrl, item.downloadId, item.localPath)

}