package com.mediapocket.android.service

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
    fun getFeed(@Url feedUrl: String): Call<String>

//    companion object Factory {
//        fun create(): RssService {
//            val retrofit = Retrofit.Builder()
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .addConverterFactory(ScalarsConverterFactory.create())
//                    .baseUrl("https://rss.itunes.apple.com")
//                    .build()
//
//            return retrofit.create(RssService::class.java)
//        }
//    }
}