package com.mediapocket.android.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

/**
 * @author Vlad Namashko
 */
object ViewUtils {

    private var DPI: Float = 1f
    private var SCREEN_SIZE: Int = -1

    fun init(context: Context) {
        val sizeCategory = context.resources.configuration.screenLayout
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)

        DPI = metrics.density
        SCREEN_SIZE = sizeCategory and Configuration.SCREENLAYOUT_SIZE_MASK

    }

    fun getDimensionSize(size: Int): Float {
        return DPI * size
    }

    fun isTablet(): Boolean {
        return SCREEN_SIZE >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun getRealScreenSize(context: Context): Point {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size)
        } else {
            try {
                size.x = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                size.y = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }

        return size
    }
}