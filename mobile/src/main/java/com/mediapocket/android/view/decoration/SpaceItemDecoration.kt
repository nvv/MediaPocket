package com.mediapocket.android.view.decoration

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * @author Vlad Namashko
 */
class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == RecyclerView.NO_POSITION) {
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