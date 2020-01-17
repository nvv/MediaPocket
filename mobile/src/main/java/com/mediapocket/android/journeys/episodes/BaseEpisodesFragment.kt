package com.mediapocket.android.journeys.episodes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mediapocket.android.R
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.DividerItemDecoration
import com.mediapocket.android.journeys.episodes.vm.EpisodesViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.base_episode_fragment.view.*
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
abstract class BaseEpisodesFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var model: EpisodesViewModel

    protected lateinit var items: androidx.recyclerview.widget.RecyclerView
    protected lateinit var loading: ProgressBar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this, viewModelFactory).get(EpisodesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.base_episode_fragment, container, false)

        items = view.episodeList
        this.loading = view.loading

        model.isLoading.observe(this, Observer {isLoading ->
            items.visibility = if (isLoading) View.GONE else View.VISIBLE
            loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        items.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL_LIST).setPadding(ViewUtils.getDimensionSize(16)))

        return view
    }


}