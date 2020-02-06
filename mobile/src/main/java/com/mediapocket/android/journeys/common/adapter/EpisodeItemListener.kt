package com.mediapocket.android.journeys.common.adapter

import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem

interface EpisodeItemListener {

    fun favouriteClicked(item: PodcastEpisodeViewItem)

    fun downloadClicked(item: PodcastEpisodeViewItem)

    fun share(item: PodcastEpisodeViewItem)

}