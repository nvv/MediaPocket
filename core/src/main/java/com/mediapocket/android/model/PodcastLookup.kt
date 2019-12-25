package com.mediapocket.android.model

import com.google.gson.JsonObject

/**
 * @author Vlad Namashko
 */
class PodcastLookup(response: JsonObject?) : Cacheable() {

    val artwork: String by lazy { response?.get("artworkUrl600")?.asString ?: "" }
    val feedUrl: String by lazy {  response?.get("feedUrl")?.asString ?: "" }
    val primaryGenreName: String by lazy {  response?.get("primaryGenreName")?.asString ?: "" }
    val artistId: String? by lazy {  response?.get("artistId")?.asString }
    val artistName: String? by lazy {  response?.get("artistName")?.asString }

    val genreIds = mutableListOf<Int>()

    init {
        response?.get("genreIds")?.asJsonArray?.forEach {
            genreIds.add(it.asInt)
        }
    }

    override fun cacheOnDisk() = true

    override fun keepFor() = 1440 * 60 * 1000 // day

}