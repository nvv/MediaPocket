package com.mediapocket.android.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.widget.TextView
import com.mediapocket.android.ItemListView
import com.mediapocket.android.R
import com.mediapocket.android.adapters.GenreListAdapter
import com.mediapocket.android.model.Genre
import kotlinx.android.synthetic.main.podcast_details_view_phone.view.*
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