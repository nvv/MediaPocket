package com.mediapocket.android.viewmodels

import com.mediapocket.android.R
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.di.MainComponentLocator
import com.mediapocket.android.model.*
import com.mediapocket.android.service.ItunesPodcastRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastViewModel : LoadableViewModel() {

    @set:Inject
    lateinit var database: AppDatabase

    private val defaultGenres = DependencyLocator.getInstance().context.getString(R.string.default_podcasts).split(",")

    init {
        MainComponentLocator.mainComponent.inject(this)
    }

    fun loadGenres(): Single<Genres> {
        return ItunesPodcastRepository.loadGenres()
    }

    fun getSubscriptions(): Single<SubscriptionsLookupResult> {
        val dao = database.subscribedPodcastDao()
        return Single.fromCallable {
            SubscriptionsLookupResult(dao.getAll())
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTop(): Single<Result> {
        return ItunesPodcastRepository.loadTopPodcasts()
    }

    fun getTop(genreId: Int, limit: Int = 10): Single<GenreResult> {
        return ItunesPodcastRepository.loadGenrePodcasts(genreId, limit)
    }

    fun getNetowrkPodcasts(networkId: String): Single<SearchResult> {
        return ItunesPodcastRepository.lookupNetworkPodcasts(networkId, limit = 100)
    }

    fun getFeatured(): Single<GenreResult> {
        return ItunesPodcastRepository.loadFeatured()
    }

    fun getDiscoverData() : Single<DiscoverData> {
        return getDiscoverData(defaultGenres)
    }

    fun getDiscoverData(podcasts: List<String>): Single<DiscoverData> {

        return doLoadingAction {
            Single.zip(loadGenres(), ItunesPodcastRepository.loadNetworks(),
                    io.reactivex.functions.BiFunction<Genres, Networks, Pair<Genres, Networks>> {
                        genres, networks -> Pair(genres, networks) }
                    ).flatMap { pair ->
                val genres = pair.first

                val fetchData = mutableMapOf<String, Single<PodcastDiscoverResult>>()
                podcasts.forEach { id ->
                    when (id) {
                        "top" -> fetchData[id] = getTop() as Single<PodcastDiscoverResult>
                        "featured" -> fetchData[id] = getFeatured() as Single<PodcastDiscoverResult>
                        else -> {
                            genres.genres[id.toInt()]?.let {
                                fetchData[id] = getTop(it.genreId) as Single<PodcastDiscoverResult>
                            }
                        }
                    }
                }

                Single.zip(fetchData.values) { array ->
                    val items = mutableMapOf<String, PodcastDiscoverResult>()
                    var i = 0
                    fetchData.keys.forEach { key ->
                        items[key] = array[i++] as PodcastDiscoverResult

                    }

                    DiscoverData(genres, items, pair.second)
                }
            }
        }
    }
}