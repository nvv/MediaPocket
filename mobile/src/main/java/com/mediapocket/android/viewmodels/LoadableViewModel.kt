package com.mediapocket.android.viewmodels

import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

/**
 * @author Vlad Namashko
 */
abstract class LoadableViewModel : ViewModel() {

    protected val loading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun loading() = loading

    fun <R> doLoadingAction(action: () -> Single<R>): Single<R> {
        loading.onNext(true)
        return action.invoke().flatMap { result ->
            loading.onNext(false)
            Single.just(result)
        }
    }
}