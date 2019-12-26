package com.mediapocket.android.journeys.details.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.model.Rss
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.repository.RssRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    private val _isSubscribed = MutableLiveData<Boolean>()
    val isSubscribed: LiveData<Boolean> = _isSubscribed

    private val _showUndo = MutableLiveData<Boolean>()
    val showUndo: LiveData<Boolean> = _showUndo

    fun load(podcast: PodcastAdapterEntry) {
        GlobalScope.launch {
            if (podcast.feedUrl().isNullOrEmpty()) {
                val lookup = GlobalScope.async { itunesPodcastRepository.lookupPodcast(podcast.id()) }.await()

                _loadPodcast.postValue(PodcastDetails(lookup.feedUrl(), lookup.artwork(), lookup.primaryGenreName(),
                        lookup.genreIds(), lookup.artistId(), lookup.artistName()))

            } else {
                _loadPodcast.postValue(PodcastDetails(podcast.feedUrl()
                        ?: "", primaryGenreName = podcast.primaryGenreName(),
                        genreIds = podcast.genreIds(), authorId = podcast.artistId().toString(), authorName = podcast.artistName()))
            }
        }
    }

    fun loadFeed(podcast: PodcastDetails) {
        GlobalScope.launch {
            _rssData.postValue(rssRepository.loadRss(podcast.feedUrl))
        }
    }

    fun isSubscribed(id: String) {
        GlobalScope.async {
            _isSubscribed.postValue(database.subscribedPodcastDao().get(id) != null)
        }
    }

    fun subscribe(podcast: PodcastAdapterEntry, details: PodcastDetails, explicitlyInvoked: Boolean = false) {
        GlobalScope.async {
            val dao = database.subscribedPodcastDao()
            if (dao.get(podcast.id()) == null) {
                dao.insertAll(SubscribedPodcast(podcast.id(), podcast.title(), podcast.logo(), details.feedUrl,
                        details.primaryGenreName, details.genreIds?.joinToString(), details.authorId, details.authorName))
            } else {
                dao.delete(podcast.id())
            }

            val isSubscribed = dao.get(podcast.id()) != null
            _isSubscribed.postValue(isSubscribed)

            if (explicitlyInvoked) {
                _showUndo.postValue(isSubscribed)
            }
        }
    }

}