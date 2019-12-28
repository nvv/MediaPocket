package com.mediapocket.android.journeys.details.viewitem

import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.model.Item

class PodcastEpisodeViewItem(
        private val item: Item,
        val rssLink: String,
        val podcastId: String?) {

    val title by lazy { item.title }

    val description by lazy { item.description }

    val pubDate by lazy { item.dateFormatted() }

    val link by lazy { item.link }

//    var download: PodcastDownloadItem? = null

    var isPlaying: Boolean = false

}