package com.mediapocket.android.viewmodels

import com.mediapocket.android.model.SearchResult
import com.mediapocket.android.service.ItunesPodcastRepository
import io.reactivex.Single

/**
 * @author Vlad Namashko
 */
class SearchPodcastViewModel : LoadableViewModel() {

    fun search(term: String): Single<SearchResult> {
        return doLoadingAction { ItunesPodcastRepository.searchPodcasts(term) }
    }
}