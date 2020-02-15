package com.mediapocket.android.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mediapocket.android.R
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.episodes.adapter.LocalEpisodesAdapter
import com.mediapocket.android.episodes.vm.DownloadedEpisodesViewModel

/**
 * @author Vlad Namashko
 */
class DownloadedEpisodesFragment : BaseEpisodesFragment<DownloadedEpisodesViewModel>() {

    var episodes: List<PodcastEpisodeViewItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        model.downloadedEpisodes.observe(viewLifecycleOwner, Observer {
            episodes = it
            if (items.adapter == null) {
                items.adapter = LocalEpisodesAdapter(it, podcastEpisodeItemListener).apply {
                    setHasStableIds(true)
                }
                (items.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
            } else {
                val adapter = items.adapter as? LocalEpisodesAdapter
                adapter?.episodes = it
                adapter?.notifyDataSetChanged()
            }

        })

        model.episodesChanged.observe(viewLifecycleOwner, Observer { changed ->
            changed.forEach {
                System.out.println(">>>> ? " + episodes?.get(it)?.title + " " + episodes?.get(it)?.downloadState?.progress)

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
//                                    (items.adapter as LocalEpisodesAdapter).onItemRemoved(event.item, event.positionInList)
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

    override fun initViewModel() {
        model = ViewModelProviders.of(this, viewModelFactory).get(DownloadedEpisodesViewModel::class.java)
    }

    override fun getTitle(): String? = getString(R.string.downloaded)

    companion object {
        fun newInstance(): DownloadedEpisodesFragment {
            return DownloadedEpisodesFragment()
        }

        const val TAG = "DownloadedEpisodesFragment"
    }
}