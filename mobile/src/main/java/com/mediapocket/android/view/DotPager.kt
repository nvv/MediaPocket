package com.mediapocket.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.mediapocket.android.R
import java.util.ArrayList

/**
 * @author Vlad Namashko
 */
class DotPager : LinearLayout {

    private var mDots: ArrayList<View>? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun initView(count: Int, current: Int) {
        removeAllViews()
        mDots = ArrayList()

        if (count == 0) {
            return
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.width = resources.getDimensionPixelOffset(R.dimen.pager_width)
        lp.height = resources.getDimensionPixelOffset(R.dimen.pager_height)
        lp.leftMargin = 2
        lp.rightMargin = 2
        for (i in 0 until count) {
            val iv = View(context)
            iv.setBackgroundResource(if (i == current) R.color.pager_selected else R.color.pager_other)
            iv.layoutParams = lp
            mDots!!.add(iv)
            addView(iv)
        }
        gravity = Gravity.CENTER
        orientation = LinearLayout.HORIZONTAL

        requestLayout()
    }

    fun setCurrentItem(current: Int) {
        if (mDots == null || mDots!!.size == 0 || current >= mDots!!.size) {
            return
        }
        val dotsCount = mDots!!.size

        val childCount = childCount
        if (childCount == 0 || childCount < current) {
            return
        }

        for (i in 0 until dotsCount) {
            val iv = mDots!![i]
            iv.setBackgroundResource(if (i == current) R.color.pager_selected else R.color.pager_other)
        }
    }

}