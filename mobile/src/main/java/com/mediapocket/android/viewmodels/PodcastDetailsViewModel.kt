package com.mediapocket.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.model.Rss
import com.mediapocket.android.model.SearchResult
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.repository.RssRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewModel @Inject constructor(
        private val database: AppDatabase,
        private val itunesPodcastRepository: ItunesPodcastRepository,
        private val rssRepository: RssRepository
) : ViewModel() {

    private val _loadPodcast = MutableLiveData<PodcastDetails>()
    val loadPodcast: LiveData<PodcastDetails> = _loadPodcast

    private val _rssData = MutableLiveData<Rss>()
    val rssData: LiveData<Rss> = _rssData

    suspend fun load(podcast: PodcastAdapterEntry) {
        if (podcast.feedUrl().isNullOrEmpty()) {
             val lookup = GlobalScope.async { itunesPodcastRepository.lookupPodcast(podcast.id()) }.await()

            _loadPodcast.postValue(PodcastDetails(lookup.feedUrl, lookup.artwork, lookup.primaryGenreName,
                    lookup.genreIds, lookup.artistId, lookup.artistName))

        } else {
            _loadPodcast.postValue(PodcastDetails(podcast.feedUrl() ?: "", primaryGenreName = podcast.primaryGenreName(),
                    genreIds = podcast.genreIds(), authorId = podcast.artistId().toString(), authorName = podcast.artistName()))
        }
    }

    suspend fun loadFeed(podcast: PodcastDetails) {
        _rssData.postValue(rssRepository.loadRss(podcast.feedUrl))
    }

    fun isSubscribed(id: String): Single<Boolean> {
        return Single.fromCallable {
            database.subscribedPodcastDao().get(id) != null
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun subscribe(podcast: PodcastAdapterEntry, details: PodcastDetails): Single<Boolean> {
        return Single.fromCallable {
            val dao = database.subscribedPodcastDao()

            if (dao.get(podcast.id()) == null) {
                dao.insertAll(SubscribedPodcast(podcast.id(), podcast.title(), podcast.logo(), details.feedUrl,
                        details.primaryGenreName, details.genreIds?.joinToString(), details.authorId, details.authorName))
            } else {
                dao.delete(podcast.id())
            }

            dao.get(podcast.id()) != null
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}