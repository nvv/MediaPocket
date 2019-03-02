package com.mediapocket.android

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.Adapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mediapocket.android.adapters.PodcastGridAdapter
import com.mediapocket.android.view.decoration.SpaceItemDecoration
import kotlinx.android.synthetic.main.podcast_list_view.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

/**
 * @author Vlad Namashko
 */
abstract class ItemListView (context: Context?) : FrameLayout(context) {

    protected var positionOnPage = 0
    protected val recyclerView: androidx.recyclerview.widget.RecyclerView
    protected val title: TextView

    init {
        val padding = dip(16)
        linearLayout {
            id = R.id.root_frame
            lparams(width = matchParent, height = matchParent)
            orientation = LinearLayout.VERTICAL
            setPadding(0, padding, 0, padding)

            textView {
                id = R.id.title
                textColorResource = R.color.black
                textSize = 18f

                setPadding(dip(16), 0, dip(16), 0)
            }.lparams(width = matchParent, height = wrapContent) {
                bottomMargin = dip(16)
            }

            recyclerView {
                id = R.id.items
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
                setHasFixedSize(true)
                addItemDecoration(SpaceItemDecoration(PodcastGridAdapter.ITEM_GAP))
            }
        }
        recyclerView = findViewById(R.id.items)

        recyclerView.id = R.id.items
        title = findViewById(R.id.title)
    }

    fun positionOnPage() : Int {
        return positionOnPage
    }

    fun getInstanceState() = recyclerView.layoutManager?.onSaveInstanceState()

    fun restoreInstanceState(state: Parcelable) {
        recyclerView.layoutManager?.onRestoreInstanceState(state)
    }

    fun scrollToBeginning() {
        recyclerView.scrollToPosition(0)
    }

}