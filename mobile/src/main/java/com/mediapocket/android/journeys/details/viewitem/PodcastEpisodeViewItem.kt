package com.mediapocket.android.journeys.details.viewitem

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

//    var download: PodcastDownloadItem? = null

    var isPlaying: Boolean = false

    var isFavourite: Boolean = false

}