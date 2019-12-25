package com.mediapocket.android.repository

import com.mediapocket.android.core.Cache
import com.mediapocket.android.core.CacheKey
import com.mediapocket.android.model.Cacheable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class CacheableRepository {

    protected fun <R : Cacheable> execCacheable(action: () -> Single<R>, key: CacheKey): Single<R> {
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

    protected suspend fun <R : Cacheable> execCacheAble(action: suspend () -> R, key: CacheKey): R {
        val cache: R? = Cache.get(key)

        cache?.let {
            return it
        } ?:
        return action.invoke().also { result ->
            Cache.put(key, result)
        }
    }

}