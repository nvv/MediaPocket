package com.mediapocket.android.model

import com.mediapocket.android.dao.model.PodcastEpisodeItem

/**
 * @author Vlad Namashko
 */
class PodcastDetails {

    var artwork: String? = null
    var feedUrl: String
    var primaryGenreName: String? = null
    var genreIds: List<Int>? = null
    var authorId: String? = null
    var authorName: String? = null
    var downloaded : List<PodcastEpisodeItem>? = null

    constructor(feedUrl: String, artwork: String? = null, primaryGenreName: String? = null,
                genreIds: List<Int>? = null, authorId: String? = null, authorName: String? = null) {
        this.feedUrl = feedUrl
        this.artwork = artwork
        this.primaryGenreName = primaryGenreName
        this.genreIds = genreIds
        this.authorId = authorId
        this.authorName = authorName
    }

}