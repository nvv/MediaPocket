package com.mediapocket.android.core.download.model

/**
 * @author Vlad Namashko
 */
data class PodcastDownloadItem (var id: String, var state: Int, var progress: Int, var podcastId: String?, var podcastTitle: String?,
                                var title: String?, var description: String?, var link: String?, var pubDate: String?,
                                var length: Long?, var imageUrl: String?, var downloadId: Int, var localPath: String?)