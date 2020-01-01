package com.mediapocket.android.core.download.manager

import android.content.Context
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration

class PodcastDownloadManager(
        private val context: Context,
        private val repository: PodcastEpisodeRepository) {



    init {
    }
}