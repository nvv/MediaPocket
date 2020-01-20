package com.mediapocket.android.journeys.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.mediapocket.android.R
import com.mediapocket.android.journeys.episodes.adapter.DownloadedEpisodesAdapter

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesFragment : BaseEpisodesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        model.downloadedEpisodes.observe(this, Observer {
            items.adapter = DownloadedEpisodesAdapter(it) { item ->
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                builder.setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_message)
                        .setPositiveButton(R.string.btn_ok) { dialog, which ->
                            run {
                                dialog.dismiss()
                                model.deleteEpisode(item)

    //                                subscription.add(model.deleteEpisode(PodcastDownloadItem(event.item)).subscribe {
    //                                    (items.adapter as DownloadedEpisodesAdapter).onItemRemoved(event.item, event.positionInList)
    //                                })
                            }
                        }
                        .setNegativeButton(R.string.btn_cancel) { dialog, which -> dialog.dismiss() }

                builder.create().show()
            }
        })

        model.episodesChanged.observe(this, Observer { changed ->
            changed.forEach {
                items.adapter?.notifyItemChanged(it)
            }
        })

        model.requestDownloadedEpisodes()


//        subscription.add(RxBus.default.observerFor(DeletePodcastEpisodeEvent::class.java).subscribe { event ->
//                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
//                builder.setTitle(R.string.confirm)
//                        .setMessage(R.string.confirm_message)
//                        .setPositiveButton(R.string.btn_ok) { dialog, which ->
//                            run {
//                                dialog.dismiss()
//                                subscription.add(model.deleteEpisode(PodcastDownloadItem(event.item)).subscribe {
//                                    (items.adapter as DownloadedEpisodesAdapter).onItemRemoved(event.item, event.positionInList)
//                                })
//                            }
//                        }
//                        .setNegativeButton(R.string.btn_cancel) { dialog, which -> dialog.dismiss() }
//
//                builder.create().show()
//
//        })

        return view
    }

    companion object {
        fun newInstance(): DownloadedEpisodesFragment {
            return DownloadedEpisodesFragment()
        }

        const val TAG = "DownloadedEpisodesFragment"
    }
}