package com.mediapocket.android.events

/**
 * @author Vlad Namashko
 */
class SwitchPodcastPlayerModeEvent(val action: Int) {

    companion object {
        const val OPEN = 0
        const val CLOSE = 1

        fun open(): SwitchPodcastPlayerModeEvent {
            return SwitchPodcastPlayerModeEvent(OPEN)
        }

        fun close(): SwitchPodcastPlayerModeEvent {
            return SwitchPodcastPlayerModeEvent(CLOSE)
        }

    }
}