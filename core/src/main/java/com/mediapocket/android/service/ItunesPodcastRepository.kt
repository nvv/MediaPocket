package com.mediapocket.android.service

import android.mediapocket.com.core.R
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mediapocket.android.core.Cache
import com.mediapocket.android.core.CacheKey
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.*
import com.mediapocket.android.utils.GlobalUtils
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
) {

    private val PODCAST_ID = 26

    private val country = GlobalUtils.getUserCountry(DependencyLocator.getInstance().context)

    private val genreKey = CacheKey(Genres::class.java, listOf(country))
    private val featuredKey = CacheKey(GenreResult::class.java, listOf(country, "featured"))
    private val topPodcastsKey = CacheKey(Result::class.java, listOf(country))

    fun loadTopPodcasts(): Single<Result> {
        return execCacheable({ topPodcastService.get(country) }, topPodcastsKey)
    }

    fun loadGenres(): Single<Genres> {
        return execCacheable({
            Single.create { emitter: SingleEmitter<Genres> ->
                searchPodcastService.genres(PODCAST_ID).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                        emitter.onError(t!!)
                    }

                    override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                        response?.body()?.get(PODCAST_ID.toString())?.let {
                            emitter.onSuccess(Genres(it.asJsonObject))
                        }
                    }
                })
            }
        }, genreKey)

    }


    fun loadGenrePodcasts(genreId: Int, limit: Int = 10): Single<GenreResult> {
        return execCacheable({
            loadGenres().flatMap { genres ->

                val genre = genres.genres[genreId]
                Single.create { emitter: SingleEmitter<GenreResult> ->
                    searchPodcastService.bestOfGenre(genreId, limit, country).enqueue(object : Callback<JsonObject> {
                        override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                            emitter.onError(t!!)
                        }

                        override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                            response?.body()?.let {
                                emitter.onSuccess(GenreResult(it.asJsonObject, genre))
                            }
                        }
                    })
                }
            }
        }, CacheKey(GenreResult::class.java, listOf(country, genreId.toString(), limit.toString())))
    }

    fun loadFeatured(): Single<GenreResult> {
        return execCacheable({
            Single.create { emitter: SingleEmitter<GenreResult> ->
                searchPodcastService.featured(country, PODCAST_ID.toString()).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                        emitter.onError(t!!)
                    }

                    override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                        response?.body()?.let {
                            emitter.onSuccess(GenreResult(it.asJsonObject).featured())
                        }
                    }
                })
            }
        }, featuredKey)
    }

    fun lookupPodcast(id: String): Single<PodcastLookup> {
        return execCacheable({
            Single.create<PodcastLookup> { emitter ->
                searchPodcastService.lookup(id).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                        emitter.onError(t!!)
                    }

                    override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                        response?.body()?.get("results")?.asJsonArray?.get(0)?.let {
                            emitter.onSuccess(PodcastLookup(it.asJsonObject))
                        }
                    }
                })
            }
        }, CacheKey(PodcastLookup::class.java, listOf(id)))
    }

    fun lookupNetworkPodcasts(id: String, limit: Int = 50): Single<SearchResult> {
        return execCacheable({ searchPodcastService.lookupItems(id, limit = limit) }, CacheKey(SearchResult::class.java, listOf(id, limit.toString()))).map { result->
            SearchResult(result.resultCount - 1, result.results.subList(1, result.results.size))
        }
    }

    fun searchPodcasts(term: String): Single<SearchResult> {
        return execCacheable({ searchPodcastService.search(term) }, CacheKey(SearchResult::class.java, listOf(term)))
    }

    fun loadNetworks(): Single<Networks> {
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
            return Single.just(Networks(networks))
        }
    }

    private fun <R : Cacheable> execCacheable(action: () -> Single<R>, key: CacheKey): Single<R> {
        val cache: R? = Cache.get(key)

        cache?.let {
            return Single.just(it)
        } ?:
            return action.invoke()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap { result: R ->
                            Cache.put(key, result)
                            Single.just(result)
                        }
    }


}