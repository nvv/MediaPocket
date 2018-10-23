package com.mediapocket.android.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.mediapocket.android.dao.model.SubscribedPodcast


/**
 * @author Vlad Namashko
 */
@Dao
interface SubscribedPodcastDao {

    @Query("SELECT * FROM podcast_subscription WHERE id=:id")
    fun get(id: String): SubscribedPodcast?

    @Query("SELECT * FROM podcast_subscription")
    fun getAll(): List<SubscribedPodcast>

    @Insert
    fun insertAll(vararg podcasts: SubscribedPodcast)

    @Query("DELETE FROM podcast_subscription WHERE id=:id")
    fun delete(id: String)
}