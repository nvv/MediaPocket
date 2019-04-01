package com.mediapocket.android.fragments

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.mediapocket.android.R
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.events.DeletePodcastEpisodeEvent
import com.mediapocket.android.extensions.getResourceIdAttribute
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.DividerItemDecoration
import com.mediapocket.android.view.decoration.DividerItemDecoration.Companion.VERTICAL_LIST
import com.mediapocket.android.viewmodels.EpisodesViewModel
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesFragment : BaseEpisodesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        subscription.add(model.getDownloadedEpisodes().subscribe {
            episodes ->
            episodes?.let {
                items.adapter = DownloadedEpisodesAdapter(it)
            }
        })


        subscription.add(RxBus.default.observerFor(DeletePodcastEpisodeEvent::class.java).subscribe { event ->
                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                builder.setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_message)
                        .setPositiveButton(R.string.btn_ok) { dialog, which ->
                            run {
                                dialog.dismiss()
                                subscription.add(model.deleteEpisode(PodcastDownloadItem(event.item)).subscribe {
                                    (items.adapter as DownloadedEpisodesAdapter).onItemRemoved(event.item, event.positionInList)
                                })
                            }
                        }
                        .setNegativeButton(R.string.btn_cancel) { dialog, which -> dialog.dismiss() }

                builder.create().show()

        })

        return view
    }

    companion object {
        fun newInstance(): DownloadedEpisodesFragment {
            return DownloadedEpisodesFragment()
        }

        const val TAG = "DownloadedEpisodesFragment"
    }
}