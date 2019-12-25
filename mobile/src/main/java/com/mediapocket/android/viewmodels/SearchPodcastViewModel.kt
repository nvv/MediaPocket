package com.mediapocket.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.model.SearchResult
import com.mediapocket.android.repository.ItunesPodcastRepository
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class SearchPodcastViewModel @Inject constructor(private val itunesPodcastRepository: ItunesPodcastRepository) : LoadableViewModel() {

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    suspend fun search(term: String) {
        _searchResult.postValue(doAction { itunesPodcastRepository.searchPodcasts(term) })
    }
}