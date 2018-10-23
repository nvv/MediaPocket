package com.mediapocket.android.view

import android.content.Context
import android.util.AttributeSet
import com.mediapocket.android.ItemListView
import com.mediapocket.android.adapters.PodcastListAdapter
import com.mediapocket.android.model.PodcastAdapterEntry

/**
 * @author Vlad Namashko
 */
class PodcastListView(context: Context?, index: Int) : ItemListView(context, index) {

    protected val adapter: PodcastListAdapter = PodcastListAdapter()

    init {
        recyclerView.adapter = adapter
    }

    fun addMoreAction(action: () -> Unit) {
        adapter.addMoreAction(action)
    }

    fun load(listTitle: String, items: List<PodcastAdapterEntry>, position: Int) {
        adapter.load(items, position)
        title.text = listTitle
    }

}