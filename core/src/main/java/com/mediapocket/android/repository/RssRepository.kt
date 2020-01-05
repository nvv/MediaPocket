package com.mediapocket.android.repository

import com.mediapocket.android.api.retrofit.RssService
import com.mediapocket.android.core.Cache
import com.mediapocket.android.core.CacheKey
import com.mediapocket.android.model.Cacheable
import com.mediapocket.android.model.Rss
import io.reactivex.Single
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
class RssRepository constructor(private val rssService: RssService) : CacheableRepository() {

    suspend fun loadRss(url: String): Rss {
         return execCacheAble({ Rss(rssService.getFeed(url), overrideLink = url) },
                CacheKey(Rss::class.java, listOf(url.replace("/", "$").replace("_", "$"))))

//        val l = execCacheAble({ rssService.getFeed(url) },
//                CacheKey(Rss::class.java, listOf(url.replace("/", "$").replace("_", "$"))))

//        return execCacheable({
//            Single.create<Rss> { emitter ->
//                rssService.getFeed(url).enqueue(object : Callback<String> {
//                    override fun onFailure(call: Call<String>?, t: Throwable?) {
//                        emitter.onError(t!!)
//                    }
//
//                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
//                        val rss = Rss(response?.body(), overrideLink = url)
//                        emitter.onSuccess(rss)
//                    }
//                })
//            }
//        }, CacheKey(Rss::class.java, listOf(url.replace("/", "$").replace("_", "$"))))
    }

}