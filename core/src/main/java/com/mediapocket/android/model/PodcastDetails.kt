package com.mediapocket.android.model

/**
 * @author Vlad Namashko
 */
class PodcastDetails {

    private var artwork: String? = null
    private var feedUrl: String
    private var primaryGenreName: String? = null
    private var genreIds: List<Int>? = null

    constructor(feedUrl: String, artwork: String? = null, primaryGenreName: String? = null, genreIds: List<Int>? = null) {
        this.feedUrl = feedUrl
        this.artwork = artwork
        this.primaryGenreName = primaryGenreName
        this.genreIds = genreIds
    }

    fun artwork(): String? = artwork

    fun feedUrl(): String = feedUrl

    fun primaryGenreName(): String? = primaryGenreName

    fun genreIds(): List<Int>? = genreIds
}