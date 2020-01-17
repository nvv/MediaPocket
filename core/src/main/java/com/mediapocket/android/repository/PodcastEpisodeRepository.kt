package com.mediapocket.android.repository

import com.mediapocket.android.dao.EpisodesDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item
import java.io.File

class PodcastEpisodeRepository(private val dao: EpisodesDao) {

    fun get(id: String): PodcastEpisodeItem? = dao.get(id)

    fun insert(episode: PodcastEpisodeItem) = dao.insert(episode)

    fun update(episode: PodcastEpisodeItem) = dao.update(episode)

    fun getDownloads() = dao.getDownloads()

    fun getFavourites(): List<PodcastEpisodeItem>? = dao.getFavourites()

    /**
     * Toggle favourite status
     */
    fun toggleFavourite(podcastId: String?, item: Item): Boolean {
        val id = PodcastEpisodeItem.convertLinkToId(item.link)
        var storedItem = dao.get(id)
        if (storedItem == null) {
            storedItem = buildDatabaseItem(podcastId, item)
            storedItem.favourite = true
            dao.insert(storedItem)
        } else {
            storedItem.favourite = !storedItem.favourite
            dao.update(storedItem)
        }

        return storedItem.favourite
    }

    fun deleteEpisode(episode: PodcastEpisodeItem) {
        dao.delete(episode.id)
        File(episode.localPath).delete()
    }

    private fun buildDatabaseItem(podcastId: String?, item: Item) =
            PodcastEpisodeItem(PodcastEpisodeItem.STATE_NONE, podcastId,
                    item.podcastTitle, item.title, item.description, item.link, System.currentTimeMillis(),
                    item.pubDate, item.length, false, item.imageUrl, 0, null)
}