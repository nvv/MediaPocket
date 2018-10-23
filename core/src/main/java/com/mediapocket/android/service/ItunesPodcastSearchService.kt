package com.mediapocket.android.service

import com.google.gson.JsonObject
import com.mediapocket.android.model.Genres
import com.mediapocket.android.model.SearchResult
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author Vlad Namashko
 */
interface ItunesPodcastSearchService {

    @GET("lookup")
    fun lookup(@Query("id") id: String): Call<JsonObject>

    @GET("lookup")
    fun lookupItems(@Query("id") id: String, @Query("entity") media: String = "podcast", @Query(value="limit") limit: Int): Single<SearchResult>

    @GET("search")
    fun search(@Query("term") query: String, @Query("media") media: String = "podcast"): Single<SearchResult>

    @GET("/WebObjects/MZStoreServices.woa/ws/genres")
    fun genres(@Query(value="id") id: Int): Call<JsonObject>

    @GET("/{country}/rss/topaudiopodcasts/limit={limit}/genre={id}/json")
    fun bestOfGenre(@Path(value="id") genreId: Int, @Path(value="limit") limit: Int, @Path(value="country") country: String): Call<JsonObject>

    @GET("/{country}/rss/toppodcasts/genre={genre}/json")
    fun featured(@Path(value="country") country: String, @Path(value="genre") genre: String): Call<JsonObject>

    /**
     * Companion object to create the Service
     */
    companion object Factory {
        fun create(): ItunesPodcastSearchService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://itunes.apple.com/")
                    .build()

            return retrofit.create(ItunesPodcastSearchService::class.java)
        }
    }

}