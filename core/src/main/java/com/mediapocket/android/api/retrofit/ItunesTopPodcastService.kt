package com.mediapocket.android.api.retrofit

import com.google.gson.JsonObject
import com.mediapocket.android.model.Cacheable
import com.mediapocket.android.model.Genre
import com.mediapocket.android.model.Result
import io.reactivex.Single
import okhttp3.Interceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author Vlad Namashko
 */
interface ItunesTopPodcastService {

//    https://itunes.apple.com/us/rss/topaudiopodcasts/json

    @GET("api/v1/{country}/podcasts/top-podcasts/all/20/explicit.json")
    suspend fun get(@Path(value="country") country: String): Result

}