package com.mediapocket.android.utils


/**
 * @author Vlad Namashko
 */
object TimeUtils {

    val ONE_SECOND: Long = 1000
    val SECONDS: Long = 60

    val ONE_MINUTE = ONE_SECOND * 60
    val MINUTES: Long = 60

    val ONE_HOUR = ONE_MINUTE * 60
    val HOURS: Long = 24

    val ONE_DAY = ONE_HOUR * 24

    fun millisToShortDHMS(duration: Long): String {
        var duration = duration
        var res = ""
        duration /= ONE_SECOND
        val seconds = (duration % SECONDS)
        duration /= SECONDS.toLong()
        val minutes = (duration % MINUTES)
        duration /= MINUTES
        val hours = (duration % HOURS).toInt()
        val days = (duration / HOURS).toInt()
        if (days == 0) {
            if (hours == 0) {
                res = String.format("%02d:%02d", minutes, seconds)
            } else {
                res = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        } else {
            res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds)
        }
        return res
    }

}