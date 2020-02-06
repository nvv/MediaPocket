package com.mediapocket.android.view

import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem

interface EpisodeItemListener {

    fun favouriteClicked(item: PodcastEpisodeViewItem)

    fun downloadClicked(item: PodcastEpisodeViewItem)

    fun share(item: PodcastEpisodeViewItem)

}