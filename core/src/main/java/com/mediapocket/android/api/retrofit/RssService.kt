package com.mediapocket.android.api.retrofit

import com.mediapocket.android.model.Rss
import com.mediapocket.android.utils.XmlNode
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


/**
 * @author Vlad Namashko
 */
interface RssService {

    @GET
    suspend fun getFeed(@Url feedUrl: String): String

}