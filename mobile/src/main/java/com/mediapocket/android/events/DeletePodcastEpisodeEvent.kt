package com.mediapocket.android.events

import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */
class DeletePodcastEpisodeEvent(val item : PodcastEpisodeItem, val positionInList: Int)