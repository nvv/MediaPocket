package com.mediapocket.android.repository

import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.dao.model.SubscribedPodcast

class PodcastRepository(private val database: AppDatabase) {

    fun isSubscribed(id: String) = database.subscribedPodcastDao().get(id) != null

    /**
     * Subscribe/unsubscribe to the <code>podcast</code>.
     *
     * If <code>podcast</code> exists in database - remove it, otherwise - add.
     *
     * @return <code>true</code> if user is subscribed to the podcast (after toggle is done), <code>false</code> otherwise.
     */
    fun toggleSubscribe(podcast: SubscribedPodcast): Boolean {
        val dao = database.subscribedPodcastDao()
        if (isSubscribed(podcast.id)) {
            dao.delete(podcast.id)
        } else {
            dao.insertAll(podcast)
        }

        return isSubscribed(podcast.id)
    }

}