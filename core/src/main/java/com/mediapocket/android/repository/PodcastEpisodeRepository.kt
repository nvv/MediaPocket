package com.mediapocket.android.repository

import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.model.Item

class PodcastEpisodeRepository(private val database: AppDatabase) {

    fun getFavourites(): List<PodcastEpisodeItem>? = database.podcastEpisodeItemDao().getFavourites()

    /**
     * Toggle favourite status
     */
    fun toggleFavourite(podcastId: String?, item: Item): Boolean {
        val dao = database.podcastEpisodeItemDao()
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