package com.mediapocket.android.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.telephony.TelephonyManager
import java.util.*

/**
 * @author Vlad Namashko
 */
object GlobalUtils {

    fun getUserCountry(context: Context) : String {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US)
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US)
                }
            }
        } catch (e: Exception) { }

        return context.resources.configuration.locale.country
    }

    fun <T : Activity> getActivity(context: Context): T? {
        if (context is Activity) {
            return context as T
        }

        return if (context is ContextWrapper) {
            getActivity(context.baseContext)
        } else null
    }

}