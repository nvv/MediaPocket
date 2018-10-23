package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.google.gson.JsonObject
import com.mediapocket.android.core.DependencyLocator

/**
 * @author Vlad Namashko
 */
class GenreResult(private val response: JsonObject, private val genre: Genre? = null) : Cacheable(), PodcastDiscoverResult {

    override fun cacheOnDisk() = true

    override fun keepFor() = 360 * 60 * 1000 // 6 hours

    val entries: MutableList<PodcastEntry> = mutableListOf()
    private var featued = false

    fun featured(): GenreResult {
        featued = true
        return this
    }

    override fun title(): String {
        return if
                (featued) DependencyLocator.getInstance().context.getString(R.string.featured)
        else

            DependencyLocator.getInstance().context.getString(R.string.top_podcasts_in,
                    genre?.name
                            ?: response.get("feed").asJsonObject.get("title").asJsonObject.get("label").asString)
    }

    init {
        val entriesJson = response.get("feed").asJsonObject.get("entry")?.asJsonArray

        entriesJson?.forEach { item ->
            val obj = item.asJsonObject

            entries.add(PodcastEntry(obj.get("id").asJsonObject.get("attributes").asJsonObject.get("im:id").asString,
                    obj.get("im:artist").asJsonObject.get("label").asString,
                    obj.get("im:releaseDate").asJsonObject.get("label").asString,
                    obj.get("im:name").asJsonObject.get("label").asString,
                    obj.get("im:image").asJsonArray.get(obj.get("im:image").asJsonArray.size() - 1).asJsonObject.get("label").asString,
                    listOf()))
        }
    }

    data class PodcastEntry(val id: String,
                            val author: String,
                            val releaseDate: String,
                            val name: String,
                            val logo: String,
                            val genreIds: List<Genre>?)

}