package com.mediapocket.android.dao

import android.arch.persistence.room.*
import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */
@Dao
interface DownloadedPodcastItemDao {

    @Query("SELECT * FROM podcast_episode_item")
    fun getAll(): List<PodcastEpisodeItem>?

    @Query("SELECT * FROM podcast_episode_item WHERE podcast_id=:podcastId")
    fun getFromPodcast(podcastId: String): List<PodcastEpisodeItem>?

    @Query("SELECT * FROM podcast_episode_item WHERE id=:id")
    fun get(id: String): PodcastEpisodeItem?

    @Insert
    fun insertAll(vararg podcasts: PodcastEpisodeItem)

    @Insert
    fun insert(podcasts: PodcastEpisodeItem)

    @Update
    fun update(podcasts: PodcastEpisodeItem)

    @Query("DELETE FROM podcast_episode_item WHERE id=:id")
    fun delete(id: String)
}