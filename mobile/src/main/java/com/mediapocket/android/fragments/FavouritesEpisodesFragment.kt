package com.mediapocket.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.adapters.DownloadedEpisodesAdapter

/**
 * @author Vlad Namashko
 */
class FavouritesEpisodesFragment : BaseEpisodesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = super.onCreateView(inflater, container, savedInstanceState)

        subscription.add(model.getFavouritesEpisodes().subscribe {
            episodes ->
            episodes?.let {
                items.adapter = DownloadedEpisodesAdapter(it)
            }
        })

        return view
    }

    companion object {
        fun newInstance(): FavouritesEpisodesFragment {
            return FavouritesEpisodesFragment()
        }

        const val TAG = "FavouritesEpisodesFragment"
    }

}