package com.mediapocket.android.journeys.details.mapper

import android.annotation.SuppressLint
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.model.Item
import java.text.SimpleDateFormat
import java.util.*

/**
 * Remote episode item to view item.
 */
class PodcastItemToEpisodeViewItemMapper {

    fun map(index: Int, item: Item, rssLink: String, podcastId: String): PodcastEpisodeViewItem =
            PodcastEpisodeViewItem(
                    position = index,
                    podcastTitle = item.podcastTitle,
                    title = item.title,
                    description = item.description,
                    pubDate = getTime(item.pubDate),
                    puDateFormatted = item.dateFormatted(),
                    link = item.link,
                    length = item.length,
                    imageUrl = item.imageUrl,
                    rssLink = rssLink,
                    podcastId = podcastId,
                    localPath = null,
                    durationFormatted = null
            )

    companion object {

        val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:mm Z", Locale.US)

        fun getTime(dateString: String): Long = dateFormatter.parse(dateString).time
    }
}