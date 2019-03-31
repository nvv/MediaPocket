package com.mediapocket.android.model

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat

/**
 * Basic playbable item - provides media metadata and media description to build exo player playlist
 * and MediaBrowserServiceCompat audio items
 *
 * @author Vlad Namashko
 */
interface PlaybackMediaDescriptor {

    fun getMediaDescription(): MediaDescriptionCompat

    fun getMediaMetadataCompat(): MediaMetadataCompat

}