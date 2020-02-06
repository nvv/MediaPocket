package com.mediapocket.android.view

import android.content.Context
import com.mediapocket.android.ItemListView
import com.mediapocket.android.view.adapters.PodcastListAdapter
import com.mediapocket.android.model.PodcastAdapterEntry

/**
 * @author Vlad Namashko
 */
class PodcastListView(context: Context?) : ItemListView(context) {

    protected val adapter: PodcastListAdapter = PodcastListAdapter()

    init {
        recyclerView.adapter = adapter
    }

    fun addMoreAction(action: () -> Unit) {
        adapter.addMoreAction(action)
    }

    fun load(listTitle: String, items: List<PodcastAdapterEntry>, positionOnPage: Int) {
        this.positionOnPage = positionOnPage
        adapter.load(items, positionOnPage)
        title.text = listTitle
    }

}