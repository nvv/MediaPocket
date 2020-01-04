package com.mediapocket.android.core.download.model

import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */
data class PodcastDownloadItem (val id: String, var state: Int, var progress: Int, val podcastId: String?,
                                val podcastTitle: String?, val title: String?, val description: String?,
                                val link: String?, val pubDate: String?, val length: Long?, val favourite: Boolean,
                                val imageUrl: String?, val downloadId: Int, val localPath: String?) {

    constructor(item: PodcastEpisodeItem) : this(item.id, item.state, 0, item.podcastId,
                item.podcastTitle, item.title, item.description, item.link,
                item.pubDate, item.length, item.favourite, item.imageUrl, item.downloadId, item.localPath)

}