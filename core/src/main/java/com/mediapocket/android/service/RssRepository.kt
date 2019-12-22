package com.mediapocket.android.service

import com.google.gson.JsonObject
import com.mediapocket.android.core.Cache
import com.mediapocket.android.core.CacheKey
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.Cacheable
import com.mediapocket.android.model.PodcastLookup
import com.mediapocket.android.model.Rss
import com.mediapocket.android.utils.XmlNode
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vlad Namashko
 */
@Singleton
class RssRepository constructor(private val rssService: RssService) {

    fun loadRss(url: String): Single<Rss> {
        return execCacheable({
            Single.create<Rss> { emitter ->
                rssService.getFeed(url).enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        emitter.onError(t!!)
                    }

                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
                        val rss = Rss(response?.body(), overrideLink = url)
                        emitter.onSuccess(rss)
                    }
                })
            }
        }, CacheKey(Rss::class.java, listOf(url.replace("/", "$").replace("_", "$"))))
    }

    private fun <R : Cacheable> execCacheable(action: () -> Single<R>, key: CacheKey): Single<R> {
        val cache: R? = Cache.get(key)

        cache?.let { it ->
            return Single.just(it)
        } ?: return action.invoke()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { result: R ->
                    Cache.put(key, result)
                    Single.just(result)
                }
    }
}