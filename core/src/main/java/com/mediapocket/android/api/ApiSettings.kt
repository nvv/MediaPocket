package com.mediapocket.android.api

import android.mediapocket.com.core.BuildConfig

object ApiSettings {

    private const val SCHEME = BuildConfig.SCHEME

    const val DEFAULT_TIMEOUT_MIN = 1L

    const val SERVER_ITUNES = SCHEME + BuildConfig.HOSTNAME_ITUNES

    const val SERVER_RSS_ITUNES = SCHEME + BuildConfig.HOSTNAME_RSS_ITUNES

}