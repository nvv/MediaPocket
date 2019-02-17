package com.mediapocket.android.playback.model

import com.mediapocket.android.model.Item

/**
 * @author Vlad Namashko
 */
abstract class PlayableItem(val mediaId: String, val link: String?) {

    companion object {
        const val MY_MEDIA_ID_DOWNLOADED = "media_id_downloaded"
    }
}

class RssEpisodeItem(itemLink: String?, rssId: String) : PlayableItem(rssId, itemLink)

class DownloadedEpisodeItem(val id: String?) : PlayableItem(MY_MEDIA_ID_DOWNLOADED, id)

