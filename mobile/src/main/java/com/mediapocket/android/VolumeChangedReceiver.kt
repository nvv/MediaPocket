package com.mediapocket.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.VolumeLevelKeyEvent

/**
 * @author Vlad Namashko
 */
class VolumeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
            RxBus.default.postEvent(RxBus.default.postEvent(VolumeLevelKeyEvent(intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0))))
        }
    }

}
