package com.mediapocket.android.viewmodels

import androidx.lifecycle.ViewModel
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.model.Rss
import com.mediapocket.android.service.ItunesPodcastRepository
import com.mediapocket.android.service.RssRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewModel @Inject constructor(
        private val database: AppDatabase,
        private val itunesPodcastRepository: ItunesPodcastRepository,
        private val rssRepository: RssRepository
) : ViewModel() {

    fun load(podcast: PodcastAdapterEntry) : Single<PodcastDetails> {
        return if (podcast.feedUrl() == null) {
            itunesPodcastRepository.lookupPodcast(podcast.id()).flatMap {
                Single.just(PodcastDetails(it.feedUrl(), it.artwork(), it.primaryGenreName(), it.genreIds(), it.artistId(), it.artistName()))
            }.observeOn(AndroidSchedulers.mainThread())
        } else {
            Single.just(PodcastDetails(podcast.feedUrl()!!, primaryGenreName = podcast.primaryGenreName(),
                    genreIds = podcast.genreIds(), authorId = podcast.artistId().toString(), authorName = podcast.artistName()))
        }
    }

    fun loadFeed(podcast: PodcastDetails) : Single<Rss> {
        return rssRepository.loadRss(podcast.feedUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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