package com.mediapocket.android.model

import com.google.gson.JsonObject

/**
 * @author Vlad Namashko
 */
class PodcastLookup(response: JsonObject) : Cacheable() {

    private val artwork: String = response.get("artworkUrl600").asString
    private val feedUrl: String = response.get("feedUrl").asString
    private val primaryGenreName: String = response.get("primaryGenreName").asString
    private val genreIds = mutableListOf<Int>()

    init {
        response.get("genreIds")?.asJsonArray?.forEach {
            genreIds.add(it.asInt)
        }
    }

    override fun cacheOnDisk() = true

    override fun keepFor() = 1440 * 60 * 1000 // day

    fun artwork() = artwork

    fun feedUrl() = feedUrl

    fun primaryGenreName() = primaryGenreName

    fun genreIds() = genreIds
}