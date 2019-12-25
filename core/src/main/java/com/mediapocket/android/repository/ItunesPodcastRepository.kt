package com.mediapocket.android.repository

import android.mediapocket.com.core.R
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mediapocket.android.api.retrofit.ItunesPodcastSearchService
import com.mediapocket.android.api.retrofit.ItunesTopPodcastService
import com.mediapocket.android.core.CacheKey
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.*
import com.mediapocket.android.utils.GlobalUtils
import io.reactivex.Single
import io.reactivex.SingleEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
class ItunesPodcastRepository constructor(
        private val topPodcastService: ItunesTopPodcastService,
        private val searchPodcastService: ItunesPodcastSearchService
) : CacheableRepository() {

    private val PODCAST_ID = 26

    private val country = GlobalUtils.getUserCountry(DependencyLocator.getInstance().context)

    private val genreKey = CacheKey(Genres::class.java, listOf(country))
    private val featuredKey = CacheKey(GenreResult::class.java, listOf(country, "featured"))
    private val topPodcastsKey = CacheKey(Result::class.java, listOf(country))

    suspend fun loadTopPodcasts(): Result {
        return execCacheAble({ topPodcastService.get(country) }, topPodcastsKey)
    }

    suspend fun loadGenres(): Genres {
        return execCacheAble({ Genres(searchPodcastService.genres(PODCAST_ID).get(PODCAST_ID.toString()).asJsonObject) }, genreKey)
    }


    suspend fun loadGenrePodcasts(genreId: Int, limit: Int = 10): GenreResult {
        val genre = loadGenres().genres[genreId]
        return execCacheAble({ GenreResult(searchPodcastService.bestOfGenre(genreId, limit, country).asJsonObject, genre) },
                CacheKey(GenreResult::class.java, listOf(country, genreId.toString(), limit.toString())))
    }

    suspend fun loadFeatured(): GenreResult {
        return execCacheAble({ GenreResult(searchPodcastService.featured(country, PODCAST_ID.toString())) }, featuredKey)
    }

    suspend fun lookupPodcast(id: String): PodcastLookup {
        return execCacheAble({ PodcastLookup(searchPodcastService.lookup(id).get("results")?.asJsonArray?.get(0)?.asJsonObject) }, CacheKey(PodcastLookup::class.java, listOf(id)))
    }

    suspend fun lookupNetworkPodcasts(id: String, limit: Int = 50): SearchResult {
        val result = execCacheAble( { searchPodcastService.lookupItems(id, limit = limit) }, CacheKey(SearchResult::class.java, listOf(id, limit.toString())))
        return SearchResult(result.resultCount - 1, result.results.subList(1, result.results.size))
    }

    suspend fun searchPodcasts(term: String): SearchResult {
        return execCacheAble({ searchPodcastService.search(term) }, CacheKey(SearchResult::class.java, listOf(term)))
    }

    suspend fun loadNetworks(): Networks {
        return withContext(Dispatchers.IO) {
            val resources = DependencyLocator.getInstance().context.resources
            val stream = resources.openRawResource(R.raw.podcast_networks).bufferedReader()
            stream.use {
                val jsonNetworks = JsonParser().parse(stream).asJsonArray
                val networks = mutableListOf<Network>()
                jsonNetworks.forEach { item ->
                    val obj = item.asJsonObject
                    networks.add(Network(obj.get("id").asString, obj.get("title").asString,
                            resources.getIdentifier(obj.get("logo").asString, "drawable", DependencyLocator.getInstance().context.packageName)))
                }
                Networks(networks)
            }
        }
    }

}