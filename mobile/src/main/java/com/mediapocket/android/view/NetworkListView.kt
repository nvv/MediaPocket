package com.mediapocket.android.view

import android.content.Context
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.view.adapters.NetworkListAdapter
import com.mediapocket.android.model.Network
import org.jetbrains.anko.textResource

/**
 * @author Vlad Namashko
 */
class NetworkListView(context: Context?) : ItemListView(context) {

    private val listAdapter = NetworkListAdapter()

    init {
        recyclerView.adapter = listAdapter
    }

    fun load(networks: List<Network>, positionOnPage: Int) {
        this.positionOnPage = positionOnPage
        title.textResource = R.string.networks
        listAdapter.load(networks)
    }

//    override fun getTitle() = R.string.networks

}