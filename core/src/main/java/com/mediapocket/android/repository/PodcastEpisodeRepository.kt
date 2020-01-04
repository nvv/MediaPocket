package com.mediapocket.android.repository

import com.mediapocket.android.dao.EpisodesDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item

class PodcastEpisodeRepository(private val dao: EpisodesDao) {

    fun get(id: String): PodcastEpisodeItem? = dao.get(id)

    fun insert(podcasts: PodcastEpisodeItem) = dao.insert(podcasts)

    fun update(podcasts: PodcastEpisodeItem) = dao.update(podcasts)

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

    private fun buildDatabaseItem(podcastId: String?, item: Item) =
            PodcastEpisodeItem(PodcastEpisodeItem.STATE_NONE, podcastId,
                    item.podcastTitle, item.title, item.description, item.link, System.currentTimeMillis(),
                    item.pubDate, item.length, false, item.imageUrl, 0, null)
}