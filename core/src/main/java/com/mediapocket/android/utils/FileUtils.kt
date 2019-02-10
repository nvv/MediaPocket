package com.mediapocket.android.utils

import java.text.DecimalFormat

/**
 * @author Vlad Namashko
 */
object FileUtils {

    /**
     * The number of bytes in a kilobyte.
     */
    const val ONE_KB: Long = 1024

    /**
     * The number of bytes in a megabyte.
     */
    const val ONE_MB = ONE_KB * ONE_KB


    private var format: DecimalFormat = DecimalFormat.getNumberInstance() as DecimalFormat
    private const val divisor: Long = 1024
    private const val suffix = "B"
    private const val nbsp = "&nbsp;"
    private val scale = arrayOf(nbsp, "K", "M", "G", "T")

    init {
        format.applyPattern("#,###.##")
    }

    /**
     * Gets formatted value (human readable string).
     *
     * @param value as long
     * @return value as string
     */
    fun formatBytes(value: Long): String {
        var scaledValue = 0f
        var scaleSuffix = scale[0]
        if (value != 0L) {
            for (i in scale.indices.reversed()) {
                val div = Math.pow(divisor.toDouble(), i.toDouble()).toLong()
                if (value >= div) {
                    scaledValue = (1.0 * value / div).toFloat()
                    scaleSuffix = scale[i]
                    break
                }
            }
        }
        val sb = StringBuilder(3)
        sb.append(format.format(scaledValue.toDouble()))

        sb.append(" ")
        if (scaleSuffix != scale[0]) {
            sb.append(scaleSuffix)
        }

        sb.append(suffix)
        return sb.toString()
    }

}