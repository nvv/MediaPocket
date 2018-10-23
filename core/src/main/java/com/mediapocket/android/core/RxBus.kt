package com.mediapocket.android.core

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

/**
 * @author Vlad Namashko
 */
class RxBus {

    private val bus = PublishProcessor.create<Any>().toSerialized()

    fun postEvent(event: Any) {
        bus.onNext(event)
    }

    fun <E : Any> observerFor(eventClass: Class<E>): Flowable<E> {
        return bus.ofType(eventClass)
    }

    companion object {
        val default = RxBus()
    }
}