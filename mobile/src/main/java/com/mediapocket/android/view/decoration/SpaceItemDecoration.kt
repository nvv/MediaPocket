package com.mediapocket.android.view.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View


/**
 * @author Vlad Namashko
 */
class SpaceItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {

        val itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
            return
        }

        val itemCount = state.itemCount
        val isFirst = itemPosition == 0
        val isLast = itemCount > 0 && itemPosition == itemCount - 1


        outRect.bottom = space
        outRect.top = space
        outRect.left = if (isFirst) 0 else space
        outRect.right = if (isLast) 0 else space
    }
}