package com.mediapocket.android.model

/**
 * @author Vlad Namashko
 */
data class DiscoverData(val genres: Genres, val podcastData: Map<String, PodcastDiscoverResult>, val networks: Networks)