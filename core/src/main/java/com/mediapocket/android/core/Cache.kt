package com.mediapocket.android.core

import com.google.gson.Gson
import com.mediapocket.android.model.Cacheable
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.Reader

/**
 * @author Vlad Namashko
 */
object Cache {

    private val cache = HashMap<CacheKey, Cacheable>()
    private val gson = Gson()

    init {
        val responses = File(DependencyLocator.getInstance().context.cacheDir.absolutePath + "/responses/")
        if (responses.exists()) {
            responses.listFiles().forEach { file ->
                val cacheMeta = file.name.split("_")
                val clazz = Class.forName(cacheMeta[1])
                val result = gson.fromJson(file.inputStream().bufferedReader(), clazz)
                if (result is Cacheable) {
                    val key = CacheKey(clazz, cacheMeta.subList(2, cacheMeta.size))
                    cache[key] = result
                    result.expires = cacheMeta[0].toLong()
                }
            }
        } else {
            responses.mkdirs()
        }
    }

    fun put(key: CacheKey, cacheable: Cacheable) {
        cache[key] = cacheable
        cacheable.cached()

        if (cacheable.cacheOnDisk()) {
            val file = "/responses/" + cacheable.expires.toString() + "_" + key.clazz.name + "_" + key.argString
            File(DependencyLocator.getInstance().context.cacheDir.absolutePath + file).printWriter().use { out ->
                out.println(gson.toJson(cacheable))
            }
        }
    }

    fun <R : Cacheable> get(key: CacheKey): R? {
        val cacheItem = cache[key]

        cacheItem?.let {
            if (it.isValid()) {
                return it as R
            } else {
                cache.remove(key)
            }
        }

        return null
    }

}

data class CacheKey(val clazz: Class<*>, val args: List<String>) {

    val argString = args.joinToString("_")

}
