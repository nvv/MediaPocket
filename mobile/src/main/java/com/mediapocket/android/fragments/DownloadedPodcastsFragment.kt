package com.mediapocket.android.fragments

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.adapters.PodcastEpisodeAdapter
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.model.PodcastDownloadItem
import com.mediapocket.android.events.DeletePodcastEpisodeEvent
import com.mediapocket.android.events.PopBackStackEvent
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.DividerItemDecoration
import com.mediapocket.android.view.decoration.DividerItemDecoration.Companion.VERTICAL_LIST
import com.mediapocket.android.viewmodels.DownloadedEpisodesViewModel
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.find

/**
 * @author Vlad Namashko
 */
class DownloadedPodcastsFragment : BaseFragment() {

    private lateinit var model: DownloadedEpisodesViewModel
    private val subscription: CompositeDisposable = CompositeDisposable()

    lateinit var items: androidx.recyclerview.widget.RecyclerView
    lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this).get(DownloadedEpisodesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = UI {
            frameLayout {
                backgroundResource = R.color.white
                lparams(width = matchParent, height = matchParent)

                items = recyclerView {

                }.lparams(width = matchParent, height = matchParent)

                loading = progressBar {
                    visibility = View.GONE
                }.lparams(width = wrapContent, height = wrapContent) {
                    gravity = Gravity.CENTER
                }
            }
        }.view

        items.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        items.addItemDecoration(DividerItemDecoration(requireActivity(), VERTICAL_LIST).setPadding(ViewUtils.getDimensionSize(16)))

        subscription.add(model.getDownloadedEpisodes().subscribe {
            episodes ->
            episodes?.let {
                items.adapter = DownloadedEpisodesAdapter(it)
            }
        })

        subscription.add(model.loading().subscribe { isLoading ->
            run {
                items.visibility = if (isLoading) View.GONE else View.VISIBLE
                loading.visibility = if (isLoading) View.VISIBLE else View.GONE
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

    override fun onDetach() {
        super.onDetach()
        subscription.dispose()
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.downloaded)

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false

    companion object {
        fun newInstance(): DownloadedPodcastsFragment {
            return DownloadedPodcastsFragment()
        }

        const val TAG = "DownloadedPodcastsFragment"
    }
}