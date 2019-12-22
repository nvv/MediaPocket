package com.mediapocket.android.di

import android.mediapocket.com.core.BuildConfig
import com.google.gson.GsonBuilder
import com.mediapocket.android.api.ApiSettings
import com.mediapocket.android.service.ItunesPodcastSearchService
import com.mediapocket.android.service.ItunesTopPodcastService
import com.mediapocket.android.service.RssService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class RetrofitModule {

    @Provides
    @Singleton
    @Named("OkHttpClient")
    fun provideClient(): OkHttpClient {
        val level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        return OkHttpClient.Builder()
            .connectTimeout(ApiSettings.DEFAULT_TIMEOUT_MIN, TimeUnit.MINUTES)
            .readTimeout(ApiSettings.DEFAULT_TIMEOUT_MIN, TimeUnit.MINUTES)
            .writeTimeout(ApiSettings.DEFAULT_TIMEOUT_MIN, TimeUnit.MINUTES)
            .addInterceptor(HttpLoggingInterceptor().setLevel(level))
            .build()
    }

    @Provides
    @Singleton
    @Named("GsonConverter")
    fun createGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().create())
    }

    @Provides
    @Singleton
    @Named("RxJava2CallAdapterFactory")
    fun createRxJava2CallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

    @Provides
    @Singleton
    @Named("ScalarsConverterFactory")
    fun createScalarsConverterFactory(): ScalarsConverterFactory {
        return ScalarsConverterFactory.create()
    }

    @Provides
    @Singleton
    @Named("ItunesRetrofit")
    fun provideItunesRetrofit(@Named("GsonConverter") gsonConverter: GsonConverterFactory,
                              @Named("ScalarsConverterFactory") scalarsConverterFactory: ScalarsConverterFactory,
                              @Named("RxJava2CallAdapterFactory") rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                              @Named("OkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(ApiSettings.SERVER_ITUNES)
                .addConverterFactory(gsonConverter)
                .addConverterFactory(scalarsConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(client)
                .build()
    }

    @Provides
    @Singleton
    @Named("RssItunesRetrofit")
    fun provideRssItunesRetrofit(@Named("GsonConverter") gsonConverter: GsonConverterFactory,
                                 @Named("ScalarsConverterFactory") scalarsConverterFactory: ScalarsConverterFactory,
                                 @Named("RxJava2CallAdapterFactory") rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                 @Named("OkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(ApiSettings.SERVER_RSS_ITUNES)
                .addConverterFactory(gsonConverter)
                .addConverterFactory(scalarsConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(client)
                .build()
    }

    @Provides
    @Singleton
    fun provideItunesPodcastSearchService(@Named("ItunesRetrofit") retrofit: Retrofit) =
            retrofit.create(ItunesPodcastSearchService::class.java)

    @Provides
    @Singleton
    fun provideItunesTopPodcastService(@Named("RssItunesRetrofit") retrofit: Retrofit) =
            retrofit.create(ItunesTopPodcastService::class.java)

    @Provides
    @Singleton
    fun provideRssService(@Named("RssItunesRetrofit") retrofit: Retrofit) =
            retrofit.create(RssService::class.java)


}