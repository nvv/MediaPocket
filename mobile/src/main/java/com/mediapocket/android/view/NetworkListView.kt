package com.mediapocket.android.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.adapters.GenreListAdapter
import com.mediapocket.android.adapters.NetworkListAdapter
import com.mediapocket.android.model.Genre
import com.mediapocket.android.model.Network
import com.mediapocket.android.model.Networks
import org.jetbrains.anko.textResource

/**
 * @author Vlad Namashko
 */
class NetworkListView(context: Context?, index: Int) : ItemListView(context, index) {

    private val listAdapter = NetworkListAdapter()

    init {
        recyclerView.adapter = listAdapter
    }

    fun load(networks: List<Network>) {
        title.textResource = R.string.networks
        listAdapter.load(networks)
    }

//    override fun getTitle() = R.string.networks

}