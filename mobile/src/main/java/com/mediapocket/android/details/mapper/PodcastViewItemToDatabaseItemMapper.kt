package com.mediapocket.android.details.mapper

import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.mapper.Mapper

/**
 * Ui item to database item
 */
class PodcastViewItemToDatabaseItemMapper : Mapper<PodcastEpisodeViewItem, PodcastEpisodeItem> {

    override fun map(item: PodcastEpisodeViewItem): PodcastEpisodeItem =
            PodcastEpisodeItem(
                    state = item.downloadState?.state ?: PodcastEpisodeItem.STATE_NONE,
                    podcastId = item.podcastId,
                    podcastTitle = item.podcastTitle,
                    rssLink = item.rssLink,
                    title = item.title,
                    description = item.description,
                    link = item.link,
                    downloadDate = item.downloadDate,
                    pubDate = item.pubDate,
                    length = item.length,
                    favourite = item.isFavourite,
                    imageUrl = item.imageUrl,
                    downloadId = 0,
                    localPath = item.localPath
            )

}