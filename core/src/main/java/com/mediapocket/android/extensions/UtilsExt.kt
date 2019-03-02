package com.mediapocket.android.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

/**
 * @author Vlad Namashko
 */
fun <T1 : Any, T2 : Any, R : Any> doubleLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun Context.getResourceIdAttribute(@AttrRes attribute: Int) : Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attribute, typedValue, true)
    return typedValue.resourceId
}