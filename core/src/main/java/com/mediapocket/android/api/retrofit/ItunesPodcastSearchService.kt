package com.mediapocket.android.api.retrofit

import com.google.gson.JsonObject
import com.mediapocket.android.model.SearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author Vlad Namashko
 */
interface ItunesPodcastSearchService {

    @GET("lookup")
    suspend fun lookup(@Query("id") id: String): JsonObject

    @GET("lookup")
    suspend fun lookupItems(@Query("id") id: String,
                            @Query("entity") media: String = "podcast",
                            @Query(value="limit") limit: Int
    ): SearchResult

    @GET("search")
    suspend fun search(@Query("term") query: String, @Query("media") media: String = "podcast"): SearchResult

    @GET("/WebObjects/MZStoreServices.woa/ws/genres")
    suspend fun genres(@Query(value="id") id: Int): JsonObject

    @GET("/{country}/rss/topaudiopodcasts/limit={limit}/genre={id}/json")
    suspend fun bestOfGenre(@Path(value="id") genreId: Int, @Path(value="limit") limit: Int, @Path(value="country") country: String): JsonObject

    @GET("/{country}/rss/toppodcasts/genre={genre}/json")
    suspend fun featured(@Path(value="country") country: String, @Path(value="genre") genre: String): JsonObject

}