package com.mediapocket.android.view.adapters

import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadGenreItemsEvent
import com.mediapocket.android.model.Genre
import com.mediapocket.android.model.Genres
import org.jetbrains.anko.*

/**
 * @author Vlad Namashko
 */
class GenreListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<GenreListAdapter.Holder>() {

    private var genres: List<Genre>? = null

    fun load(genres : List<Genre>) {
        this.genres = genres
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(TextView(parent.context).apply {
            textSize = 18f
            textColorResource = R.color.black
            isClickable = true

            backgroundResource = R.drawable.round_background
            layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
        })
    }

    override fun getItemCount() : Int = genres?.size ?: 0

    override fun onBindViewHolder(holder: Holder, position: Int) {
        genres?.let {
            val genre = it[position]
            val text = holder.textView
            text.text = genre.name

            val drawable = holder.textView.background

            val color = ContextCompat.getColor(text.context, Genres.getColor(genre.genreId))
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

            text.setOnClickListener {
                RxBus.default.postEvent(LoadGenreItemsEvent(genre.genreId))
            }
        }
    }

    class Holder(val textView: TextView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(textView)
}