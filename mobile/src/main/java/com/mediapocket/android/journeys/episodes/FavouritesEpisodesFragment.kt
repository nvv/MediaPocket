package com.mediapocket.android.journeys.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mediapocket.android.R
import com.mediapocket.android.journeys.episodes.adapter.LocalEpisodesAdapter
import com.mediapocket.android.journeys.episodes.vm.FavouritesEpisodesViewModel

/**
 * @author Vlad Namashko
 */
class FavouritesEpisodesFragment : BaseEpisodesFragment<FavouritesEpisodesViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        model.favouriteEpisodes.observe(viewLifecycleOwner, Observer {
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
                items.adapter?.notifyItemChanged(it)
            }
        })

        model.requestFavouritesEpisodes()

        return view
    }

    override fun initViewModel() {
        model = ViewModelProviders.of(this, viewModelFactory).get(FavouritesEpisodesViewModel::class.java)
    }

    override fun getTitle(): String? = getString(R.string.favourites)

    companion object {
        fun newInstance(): FavouritesEpisodesFragment {
            return FavouritesEpisodesFragment()
        }

        const val TAG = "FavouritesEpisodesFragment"
    }

}