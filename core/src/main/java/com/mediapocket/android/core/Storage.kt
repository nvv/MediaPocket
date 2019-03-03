package com.mediapocket.android.core

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * @author Vlad Namashko
 */
class Storage (context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCE, Activity.MODE_PRIVATE)

    var showAnimationLogo: Boolean
        get() = preferences.getBoolean(SHOW_ANIMATION_LOGO, true)
        set(value) = preferences.edit().putBoolean(SHOW_ANIMATION_LOGO, value).apply()

    companion object {
        private const val PREFERENCE = "PREFERENCE"
        private const val SHOW_ANIMATION_LOGO = "SHOW_ANIMATION_LOGO"
    }
}