package com.mediapocket.android.fragments

import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import com.mediapocket.android.R
import com.mediapocket.android.view.EpisodeItemListener
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.details.viewitem.isDownloaded
import com.mediapocket.android.details.viewitem.isDownloading
import com.mediapocket.android.details.viewitem.isError
import com.mediapocket.android.viewmodels.PlaybackStateAwareViewModel

abstract class BasePodcastEpisodeFragment<T : PlaybackStateAwareViewModel>: BaseFragment() {

    protected lateinit var model: T

    protected val podcastEpisodeItemListener: EpisodeItemListener = object : EpisodeItemListener {
        override fun statusClicked(item: PodcastEpisodeViewItem) {
            // TODO: move to VM
            if (item.isDownloaded) {
                confirmDelete(item)
            } else if (item.downloadState == null || item.isError) {
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

    private fun confirmDelete(item: PodcastEpisodeViewItem) {
        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle(R.string.confirm)
                .setMessage(R.string.confirm_message)
                .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                    run {
                        dialog.dismiss()
                        model.deleteEpisode(item)
                    }
                }
                .setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}