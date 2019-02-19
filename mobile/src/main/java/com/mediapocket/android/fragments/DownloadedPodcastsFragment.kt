package com.mediapocket.android.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter
import com.mediapocket.android.core.DependencyLocator
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

    lateinit var items: RecyclerView
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

        items.layoutManager = LinearLayoutManager(context)
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