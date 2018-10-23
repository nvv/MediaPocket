package com.mediapocket.android.viewmodels

import android.arch.lifecycle.ViewModel
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.model.Rss
import com.mediapocket.android.service.ItunesPodcastRepository
import com.mediapocket.android.service.RssRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewModel : ViewModel() {

    fun load(podcast: PodcastAdapterEntry) : Single<PodcastDetails> {
        return if (podcast.feedUrl() == null) {
            ItunesPodcastRepository.lookupPodcast(podcast.id()).flatMap {
                Single.just(PodcastDetails(it.feedUrl(), it.artwork(), it.primaryGenreName(), it.genreIds()))
            }.observeOn(AndroidSchedulers.mainThread())
        } else {
            Single.just(PodcastDetails(podcast.feedUrl()!!, primaryGenreName = podcast.primaryGenreName(), genreIds = podcast.genreIds()))
        }
    }

    fun loadFeed(podcast: PodcastDetails) : Single<Rss> {
        return RssRepository.loadRss(podcast.feedUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun isSubscribed(id: String): Single<Boolean> {
        return Single.fromCallable {
            DependencyLocator.getInstance().database.subscribedPodcastDao().get(id) != null
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun subscribe(podcast: PodcastAdapterEntry, details: PodcastDetails): Single<Boolean> {
        return Single.fromCallable {
            val dao = DependencyLocator.getInstance().database.subscribedPodcastDao()

            if (dao.get(podcast.id()) == null) {
                dao.insertAll(SubscribedPodcast(podcast.id(), podcast.title(), podcast.logo(), details.feedUrl()))
            } else {
                dao.delete(podcast.id())
            }

            dao.get(podcast.id()) != null
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}