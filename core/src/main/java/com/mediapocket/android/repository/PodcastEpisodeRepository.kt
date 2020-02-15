package com.mediapocket.android.repository

import com.mediapocket.android.dao.EpisodesDao
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.io.File

class PodcastEpisodeRepository(private val dao: EpisodesDao) {

    fun get(id: String): PodcastEpisodeItem? = dao.get(id)

    fun insert(episode: PodcastEpisodeItem) = dao.insert(episode)

    fun update(episode: PodcastEpisodeItem) = dao.update(episode)

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
            dao.insert(storedItem)
        } else {
            storedItem.favourite = !storedItem.favourite
            dao.update(storedItem)
        }

        episodes.send(true)
        return storedItem.favourite
    }

    suspend fun deleteEpisode(episodeId: String) {
        val episode = dao.get(episodeId)
        episode?.let {
            File(it.localPath).delete()

            if (!it.favourite) {
                dao.delete(episodeId)
            } else {
                it.downloadDate = -1
                it.localPath = null
                it.state = PodcastEpisodeItem.STATE_NONE
                dao.update(it)
            }
        }

        episodes.send(true)
    }

}