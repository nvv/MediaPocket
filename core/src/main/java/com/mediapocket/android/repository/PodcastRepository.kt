package com.mediapocket.android.repository

import com.mediapocket.android.dao.SubscribedPodcastDao
import com.mediapocket.android.dao.model.SubscribedPodcast

class PodcastRepository(private val dao: SubscribedPodcastDao) {

    fun isSubscribed(id: String) = dao.get(id) != null

    /**
     * Subscribe/unsubscribe to the <code>podcast</code>.
     *
     * If <code>podcast</code> exists in database - remove it, otherwise - add.
     *
     * @return <code>true</code> if user is subscribed to the podcast (after toggle is done), <code>false</code> otherwise.
     */
    fun toggleSubscribe(podcast: SubscribedPodcast): Boolean {
        if (isSubscribed(podcast.id)) {
            dao.delete(podcast.id)
        } else {
            dao.insertAll(podcast)
        }

        return isSubscribed(podcast.id)
    }

}