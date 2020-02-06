package com.mediapocket.android.journeys.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.mediapocket.android.R
import com.mediapocket.android.journeys.common.BasePodcastEpisodeFragment
import com.mediapocket.android.journeys.episodes.vm.EpisodesViewModel
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.DividerItemDecoration
import kotlinx.android.synthetic.main.base_episode_fragment.view.*

/**
 * @author Vlad Namashko
 */
abstract class BaseEpisodesFragment<T : EpisodesViewModel>: BasePodcastEpisodeFragment<T>() {

    protected lateinit var items: RecyclerView
    protected lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.base_episode_fragment, container, false)

        items = view.episodeList
        this.loading = view.loading

        model.isLoading.observe(viewLifecycleOwner, Observer {isLoading ->
            items.visibility = if (isLoading) View.GONE else View.VISIBLE
            loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        items.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL_LIST).setPadding(ViewUtils.getDimensionSize(16)))

        return view
    }

    protected abstract fun initViewModel()

    override fun hasNavigation(): Boolean = true

    override fun hasBackNavigation(): Boolean = false

}