package com.mediapocket.android.core.download.model

import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */
data class PodcastDownloadItem (var id: String, var state: Int, var progress: Int, var podcastId: String?, var podcastTitle: String?,
                                var title: String?, var description: String?, var link: String?, var pubDate: String?,
                                var length: Long?, var favourite: Boolean, var imageUrl: String?, var downloadId: Int, var localPath: String?) {

    constructor(item: PodcastEpisodeItem) : this(item.id, item.state, 0, item.podcastId,
                item.podcastTitle, item.title, item.description, item.link,
                item.pubDate, item.length, item.favourite, item.imageUrl, item.downloadId, item.localPath)

}