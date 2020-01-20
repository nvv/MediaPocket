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
    fun toggleFavourite(item: PodcastEpisodeItem): Boolean {
        val id = PodcastEpisodeItem.convertLinkToId(item.link)
        var storedItem = dao.get(id)
        if (storedItem == null) {
            storedItem = item
            storedItem.favourite = true
            dao.insert(storedItem)
        } else {
            storedItem.favourite = !storedItem.favourite
            dao.update(storedItem)
        }

        return storedItem.favourite
    }

    fun deleteEpisode(episodeId: String) {
        dao.get(episodeId)?.let {
            File(it.localPath).delete()
        }
        dao.delete(episodeId)
    }

}