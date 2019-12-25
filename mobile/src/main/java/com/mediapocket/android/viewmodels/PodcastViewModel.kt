package com.mediapocket.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.R
import com.mediapocket.android.core.AppDatabase
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.*
import com.mediapocket.android.repository.ItunesPodcastRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastViewModel @Inject constructor(
        private val database: AppDatabase,
        private val itunesPodcastRepository: ItunesPodcastRepository
) : LoadableViewModel() {

    private val defaultGenres = DependencyLocator.getInstance().context.getString(R.string.default_podcasts).split(",")

    private val _getDiscoverData = MutableLiveData<DiscoverData>()
    val getDiscoverData: LiveData<DiscoverData> = _getDiscoverData

    private val _getNetworkPodcasts = MutableLiveData<SearchResult>()
    val getNetworkPodcasts: LiveData<SearchResult> = _getNetworkPodcasts

    private val _genreTopPodcasts = MutableLiveData<GenreResult>()
    val genreTopPodcasts: LiveData<GenreResult> = _genreTopPodcasts

    fun getSubscriptions(): Single<SubscriptionsLookupResult> {
        val dao = database.subscribedPodcastDao()
        return Single.fromCallable {
            SubscriptionsLookupResult(dao.getAll())
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private suspend fun getTopPodcasts(): Result {
        return itunesPodcastRepository.loadTopPodcasts()
    }

    suspend fun requestTop(genreId: Int, limit: Int = 10) {
        _genreTopPodcasts.postValue(getTop(genreId, limit))
    }

    private suspend fun getTop(genreId: Int, limit: Int = 10): GenreResult {
        return itunesPodcastRepository.loadGenrePodcasts(genreId, limit)
    }

    suspend fun getNetowrkPodcasts(networkId: String) {
        _getNetworkPodcasts.postValue(doAction { itunesPodcastRepository.lookupNetworkPodcasts(networkId, limit = 100) })
    }

    private suspend fun getFeatured(): GenreResult {
        return itunesPodcastRepository.loadFeatured()
    }

    suspend fun discoverData() {
        _getDiscoverData.postValue(doAction { getDiscoverData(defaultGenres) })
    }

    private suspend fun getDiscoverData(podcasts: List<String>): DiscoverData {

        val genres = GlobalScope.async { itunesPodcastRepository.loadGenres() }.await()
        val networks = GlobalScope.async {itunesPodcastRepository.loadNetworks() }

        val fetchData = mutableMapOf<String, Deferred<PodcastDiscoverResult>>()
        podcasts.forEach { id ->
            when (id) {
                "top" -> fetchData[id] = GlobalScope.async { getTopPodcasts() }
                "featured" -> fetchData[id] = GlobalScope.async { getFeatured() }
                else -> {
                    genres.genres[id.toInt()]?.let {
                        fetchData[id] = GlobalScope.async { getTop(it.genreId) }
                    }
                }
            }
        }

        return DiscoverData(genres, fetchData.mapValues { entry -> entry.value.await() }, networks.await())

//        return doLoadingAction {
//            Single.zip(loadGenres(), itunesPodcastRepository.loadNetworks(),
//                    io.reactivex.functions.BiFunction<Genres, Networks, Pair<Genres, Networks>> {
//                        genres, networks -> Pair(genres, networks) }
//                    ).flatMap { pair ->
//                val genres = pair.first
//
//                val fetchData = mutableMapOf<String, Single<PodcastDiscoverResult>>()
//                podcasts.forEach { id ->
//                    when (id) {
//                        "top" -> fetchData[id] = getTopPodcasts() as Single<PodcastDiscoverResult>
//                        "featured" -> fetchData[id] = getFeatured() as Single<PodcastDiscoverResult>
//                        else -> {
//                            genres.genres[id.toInt()]?.let {
//                                fetchData[id] = getTop(it.genreId) as Single<PodcastDiscoverResult>
//                            }
//                        }
//                    }
//                }
//
//                Single.zip(fetchData.values) { array ->
//                    val items = mutableMapOf<String, PodcastDiscoverResult>()
//                    var i = 0
//                    fetchData.keys.forEach { key ->
//                        items[key] = array[i++] as PodcastDiscoverResult
//
//                    }
//
//                    DiscoverData(genres, items, pair.second)
//                }
//            }
//        }
    }
}