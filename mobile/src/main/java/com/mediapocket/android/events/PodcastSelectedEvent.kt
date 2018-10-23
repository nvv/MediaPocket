package com.mediapocket.android.events

import android.widget.ImageView
import com.mediapocket.android.model.PodcastAdapterEntry

/**
 * @author Vlad Namashko
 */
class PodcastSelectedEvent(val podcast: PodcastAdapterEntry, val logo: ImageView)