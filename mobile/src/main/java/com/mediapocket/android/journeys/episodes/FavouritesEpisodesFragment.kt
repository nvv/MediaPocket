package com.mediapocket.android.journeys.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.mediapocket.android.R
import com.mediapocket.android.journeys.details.adapter.DownloadedEpisodesAdapter

/**
 * @author Vlad Namashko
 */
class FavouritesEpisodesFragment : BaseEpisodesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        model.favouriteEpisodes.observe(this, Observer {
            items.adapter = DownloadedEpisodesAdapter(it)
        })

        model.requestFavouritesEpisodes()

        return view
    }

    companion object {
        fun newInstance(): FavouritesEpisodesFragment {
            return FavouritesEpisodesFragment()
        }

        const val TAG = "FavouritesEpisodesFragment"
    }

}