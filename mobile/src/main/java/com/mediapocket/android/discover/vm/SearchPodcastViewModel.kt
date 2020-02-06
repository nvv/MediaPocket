package com.mediapocket.android.discover.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.model.SearchResult
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.viewmodels.LoadableViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class SearchPodcastViewModel @Inject constructor(private val itunesPodcastRepository: ItunesPodcastRepository) : LoadableViewModel() {

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    fun search(term: String) {
        GlobalScope.launch {
            _searchResult.postValue(doAction { itunesPodcastRepository.searchPodcasts(term) })
        }
    }
}