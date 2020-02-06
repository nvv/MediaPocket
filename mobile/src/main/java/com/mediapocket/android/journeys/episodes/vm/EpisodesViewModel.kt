package com.mediapocket.android.journeys.episodes.vm

import android.content.Context
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.journeys.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.journeys.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.journeys.common.adapter.PlaybackStateAwareViewModel
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
open class EpisodesViewModel @Inject constructor(
        context: Context,
        errorMapper: DownloadErrorToStringMapper,
        viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        podcastEpisodeRepository: PodcastEpisodeRepository,
        downloadManager: PodcastDownloadManager
) : PlaybackStateAwareViewModel(downloadManager, podcastEpisodeRepository, errorMapper, viewItemToDatabaseItemMapper) {

    init {
        initMediaCallback(context)
    }

}