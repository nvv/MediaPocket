package com.mediapocket.android.journeys.episodes.viewitem

import android.media.MediaMetadataRetriever
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem.Companion.STATE_DOWNLOADED
import com.mediapocket.android.dao.model.PodcastEpisodeItem.Companion.STATE_NONE
import com.mediapocket.android.journeys.details.viewitem.DownloadState
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.utils.TimeUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EpisodeDatabaseItemToViewItem {

    fun map(index: Int, item: PodcastEpisodeItem): PodcastEpisodeViewItem =
            PodcastEpisodeViewItem(
                    position = index,
                    podcastTitle = item.podcastTitle,
                    title = item.title,
                    description = item.description,
                    pubDate = item.pubDate,
                    puDateFormatted = formatDate(item.pubDate),
                    link = item.link,
                    length = read(item.length),
                    imageUrl = item.imageUrl,
                    rssLink = item.rssLink,
                    podcastId = item.podcastId,
                    localPath = item.localPath,
                    durationFormatted = TimeUtils.millisToShortDHMS(formatDuration(item) ?: 0)
            ).apply {
                if (item.state != STATE_NONE) {
                    downloadState = DownloadState(
                            isDownloaded = item.state == STATE_DOWNLOADED
                    )
                }
            }

    private fun read(value: String?) = value ?: ""

    private fun read(value: Long?) = value ?: 0

    private fun formatDate(timestamp: Long?): String {
        return if (timestamp != null) dateFormatter.format(timestamp) else ""
    }

    private fun formatDuration(item: PodcastEpisodeItem): Long? {
        return try {
            metaRetriever.setDataSource(item.localPath)
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        } catch (ex: Exception) {
            item.length
        }
    }


    companion object {

        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        private val metaRetriever = MediaMetadataRetriever()
    }
}