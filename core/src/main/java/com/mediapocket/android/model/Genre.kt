package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.google.gson.JsonObject
import java.util.ArrayList

/**
 * @author Vlad Namashko
 */

class Genres(response: JsonObject) : Cacheable() {

    override fun cacheOnDisk() = true

    override fun keepFor() = 4320 * 60 * 1000 // 3 days

    val genres: MutableMap<Int, Genre> = mutableMapOf()

    init {
        fill(response, genres)
    }

    private fun fill(response: JsonObject, genres: MutableMap<Int, Genre>) {
        val podcasts = response.get("subgenres")?.asJsonObject?.entrySet()
        podcasts?.forEach {
            val obj = it.value.asJsonObject

            var subgenres = if (obj.has("subgenres")) mutableMapOf<Int, Genre>() else null
            subgenres?.let {
                fill(obj, subgenres)
            }

            val id = obj.get("id").asInt
            genres[id] = Genre(obj.get("id").asInt, obj.get("name").asString, obj.get("url").asString, subgenres)
        }
    }

    companion object {
        private val colors = mapOf(1318 to R.color.cooper,
                1315 to R.color.cooper,
                1316 to R.color.orange,
                1304 to R.color.silver,
                1325 to R.color.green_light,
                1324 to R.color.green_light,
                1307 to R.color.beige,
                1314 to R.color.beige,
                1311 to R.color.red,
                1310 to R.color.red_light,
                1301 to R.color.red_light,
                1309 to R.color.red_light,
                1305 to R.color.yellow,
                1321 to R.color.blue_light,
                1323 to R.color.purple,
                1303 to R.color.yellow)

        fun getColor(id : Int) : Int {
            return colors[id] ?: R.color.genre_default
        }
    }
}

data class Genre(val genreId: Int, val name: String, val url: String, val subgenres: MutableMap<Int, Genre>?)
