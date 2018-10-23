package com.mediapocket.android.model

/**
 * @author Vlad Namashko
 */
interface CacheEntity {

    var expires: Long

    fun keepFor() = 5 * 60 * 1000 // 5 minutes

    fun cacheOnDisk() = false

    fun isValid() = System.currentTimeMillis() < expires
}

abstract class Cacheable : CacheEntity {

    override var expires = 0L

    fun cached() {
        expires = System.currentTimeMillis() + keepFor()
    }
}