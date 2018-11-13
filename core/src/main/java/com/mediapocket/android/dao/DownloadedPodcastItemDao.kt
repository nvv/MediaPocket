package com.mediapocket.android.dao

import android.arch.persistence.room.*
import com.mediapocket.android.dao.model.DownloadedPodcastItem

/**
 * @author Vlad Namashko
 */
@Dao
interface DownloadedPodcastItemDao {

    @Query("SELECT * FROM downloaded_podcast_item")
    fun getAll(): List<DownloadedPodcastItem>?

    @Query("SELECT * FROM downloaded_podcast_item WHERE podcast_id=:podcastId")
    fun getFromPodcast(podcastId: String): List<DownloadedPodcastItem>?

    @Query("SELECT * FROM downloaded_podcast_item WHERE id=:id")
    fun get(id: String): DownloadedPodcastItem?

    @Insert
    fun insertAll(vararg podcasts: DownloadedPodcastItem)

    @Insert
    fun insert(podcasts: DownloadedPodcastItem)

    @Update
    fun update(podcasts: DownloadedPodcastItem)

    @Query("DELETE FROM downloaded_podcast_item WHERE id=:id")
    fun delete(id: String)
}