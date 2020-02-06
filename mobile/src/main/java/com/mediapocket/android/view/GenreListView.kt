package com.mediapocket.android.view

import android.content.Context
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.view.adapters.GenreListAdapter
import com.mediapocket.android.model.Genre
import org.jetbrains.anko.textResource

/**
 * @author Vlad Namashko
 */
class GenreListView(context: Context?) : ItemListView(context) {

    private val listAdapter = GenreListAdapter()

    init {
        recyclerView.adapter = listAdapter
        title.textResource = R.string.all_genres
    }

    fun load(genres: List<Genre>, positionOnPage: Int) {
        this.positionOnPage = positionOnPage
        listAdapter.load(genres)
    }

}