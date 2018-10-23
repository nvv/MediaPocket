package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.mediapocket.android.core.DependencyLocator

/**
 * @author Vlad Namashko
 */
data class Network(val id: String, val title: String, val logo: Int)

data class Networks(val networks: List<Network>)