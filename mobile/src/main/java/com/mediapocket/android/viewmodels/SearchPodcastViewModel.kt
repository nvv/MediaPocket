package com.mediapocket.android.viewmodels

import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.model.SearchResult
import com.mediapocket.android.service.ItunesPodcastRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class SearchPodcastViewModel @Inject constructor(private val itunesPodcastRepository: ItunesPodcastRepository) : LoadableViewModel() {

    fun search(term: String): Single<SearchResult> {
        return doLoadingAction { itunesPodcastRepository.searchPodcasts(term) }
    }
}