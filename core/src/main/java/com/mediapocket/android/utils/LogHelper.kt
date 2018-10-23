package com.mediapocket.android.utils

import android.mediapocket.com.core.BuildConfig
import android.util.Log

/**
 * @author Vlad Namashko
 */
object LogHelper {

    private val LOG_PREFIX = "mediapocket_"
    private val LOG_PREFIX_LENGTH = LOG_PREFIX.length
    private val MAX_LOG_TAG_LENGTH = 23

    fun makeLogTag(str: String): String {
        return if (str.length > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1)
        } else LOG_PREFIX + str

    }

    /**
     * Don't use this when obfuscating class names!
     */
    fun makeLogTag(cls: Class<*>): String {
        return makeLogTag(cls.simpleName)
    }


    fun v(tag: String, vararg messages: Any) {
        // Only log VERBOSE if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.VERBOSE, null, *messages)
        }
    }

    fun d(tag: String, vararg messages: Any) {
        // Only log DEBUG if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, *messages)
        }
    }

    fun i(tag: String, vararg messages: Any) {
        log(tag, Log.INFO, null, *messages)
    }

    fun w(tag: String, vararg messages: Any) {
        log(tag, Log.WARN, null, *messages)
    }

    fun w(tag: String, t: Throwable, vararg messages: Any) {
        log(tag, Log.WARN, t, *messages)
    }

    fun e(tag: String, vararg messages: Any) {
        log(tag, Log.ERROR, null, *messages)
    }

    fun e(tag: String, t: Throwable, vararg messages: Any) {
        log(tag, Log.ERROR, t, *messages)
    }

    fun log(tag: String, level: Int, t: Throwable?, vararg messages: Any) {
        if (Log.isLoggable(tag, level)) {
            val message: String
            if (t == null && messages != null && messages.size == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString()
            } else {
                val sb = StringBuilder()
                if (messages != null)
                    for (m in messages) {
                        sb.append(m)
                    }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t))
                }
                message = sb.toString()
            }
            Log.println(level, tag, message)
        }
    }
}