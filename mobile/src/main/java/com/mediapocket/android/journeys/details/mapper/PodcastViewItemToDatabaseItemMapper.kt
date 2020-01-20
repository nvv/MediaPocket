package com.mediapocket.android.journeys.details.mapper

import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.mapper.Mapper

/**
 * Ui item to database item
 */
class PodcastViewItemToDatabaseItemMapper : Mapper<PodcastEpisodeViewItem, PodcastEpisodeItem> {

    override fun map(item: PodcastEpisodeViewItem): PodcastEpisodeItem =
            PodcastEpisodeItem(
                    state = PodcastEpisodeItem.STATE_NONE,
                    podcastId = item.podcastId,
                    podcastTitle = item.podcastTitle,
                    rssLink = item.rssLink,
                    title = item.title,
                    description = item.description,
                    link = item.link,
                    downloadDate = System.currentTimeMillis(),
                    pubDate = item.pubDate,
                    length = item.length,
                    favourite = item.isFavourite,
                    imageUrl = item.imageUrl,
                    downloadId = 0,
                    localPath = null
            )

}