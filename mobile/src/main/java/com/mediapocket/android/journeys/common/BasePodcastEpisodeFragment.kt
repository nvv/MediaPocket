package com.mediapocket.android.journeys.common

import androidx.core.app.ShareCompat
import com.mediapocket.android.R
import com.mediapocket.android.fragments.BaseFragment
import com.mediapocket.android.journeys.common.adapter.EpisodeItemListener
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.journeys.details.viewitem.isDownloading
import com.mediapocket.android.journeys.details.viewitem.isError
import com.mediapocket.android.journeys.common.adapter.PlaybackStateAwareViewModel

abstract class BasePodcastEpisodeFragment<T : PlaybackStateAwareViewModel>: BaseFragment() {

    protected lateinit var model: T

    protected val podcastEpisodeItemListener: EpisodeItemListener = object : EpisodeItemListener {
        override fun downloadClicked(item: PodcastEpisodeViewItem) {
            if (item.downloadState == null || item.isError) {
                model.downloadItem(item)
            } else {
                if (item.isDownloading) {
                    model.pauseDownload(item)
                } else {
                    model.resumeDownload(item)
                }
            }

        }

        override fun share(item: PodcastEpisodeViewItem) {
            ShareCompat.IntentBuilder.from(requireActivity())
                    .setText(item.link)
                    .setSubject(item.title)
                    .setType("text/plain")
                    .setChooserTitle(R.string.sharing)
                    .startChooser()
        }

        override fun favouriteClicked(item: PodcastEpisodeViewItem) {
            model.favouriteEpisode(item)
        }

    }
}