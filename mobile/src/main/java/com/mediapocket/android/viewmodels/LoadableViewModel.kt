package com.mediapocket.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * @author Vlad Namashko
 */
abstract class LoadableViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected val loading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun loading() = loading

    fun <R> doLoadingAction(action: () -> Single<R>): Single<R> {
        loading.onNext(true)
        return action.invoke().flatMap { result ->
            loading.onNext(false)
            Single.just(result)
        }
    }

    protected suspend fun <T> doAction(action: suspend () -> T): T {
        _isLoading.postValue(true)
        val value = GlobalScope.async {
            action.invoke()
        }.await()
        _isLoading.postValue(false)
        return value
    }

}