package com.mediapocket.android.repository

import com.mediapocket.android.dao.EpisodesDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.io.File

class PodcastEpisodeRepository(private val dao: EpisodesDao) {

    fun get(id: String): PodcastEpisodeItem? = dao.get(id)

    suspend fun insert(episode: PodcastEpisodeItem) {
        dao.insert(episode)
        episodes.send(true)
    }

    suspend fun update(episode: PodcastEpisodeItem) {
        dao.update(episode)
        episodes.send(true)
    }

    fun getDownloads() = dao.getDownloads()

    fun getFavourites(): List<PodcastEpisodeItem>? = dao.getFavourites()

    val episodes = BroadcastChannel<Boolean>(Channel.CONFLATED)


    /**
     * Toggle favourite status
     */
    suspend fun toggleFavourite(item: PodcastEpisodeItem): Boolean {
        val id = PodcastEpisodeItem.convertLinkToId(item.link)
        var storedItem = dao.get(id)
        if (storedItem == null) {
            storedItem = item
            storedItem.favourite = true
            insert(storedItem)
        } else {
            storedItem.favourite = !storedItem.favourite
            update(storedItem)
        }

        return storedItem.favourite
    }

    suspend fun deleteEpisode(episodeId: String) {
        val episode = dao.get(episodeId)
        episode?.let {
            File(it.localPath).delete()

            if (!it.favourite) {
                dao.delete(episodeId)
                episodes.send(true)
            } else {
                it.downloadDate = -1
                it.localPath = null
                it.state = PodcastEpisodeItem.STATE_NONE
                update(it)
            }
        }
    }

}