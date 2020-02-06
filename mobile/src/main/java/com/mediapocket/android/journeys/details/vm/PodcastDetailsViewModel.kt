package com.mediapocket.android.journeys.details.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.dao.model.PodcastEpisodeItem.Companion.STATE_DOWNLOADED
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.journeys.details.mapper.DownloadErrorToStringMapper
import com.mediapocket.android.journeys.details.mapper.PodcastItemToEpisodeViewItemMapper
import com.mediapocket.android.journeys.details.mapper.PodcastViewItemToDatabaseItemMapper
import com.mediapocket.android.journeys.details.viewitem.DownloadState
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.repository.PodcastRepository
import com.mediapocket.android.repository.RssRepository
import com.mediapocket.android.journeys.common.adapter.PlaybackStateAwareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewModel @Inject constructor(
        private val context: Context,
        private val remoteToViewItemMapper: PodcastItemToEpisodeViewItemMapper,
        private val errorMapper: DownloadErrorToStringMapper,
        private val viewItemToDatabaseItemMapper: PodcastViewItemToDatabaseItemMapper,
        private val downloadManager: PodcastDownloadManager,
        private val itunesPodcastRepository: ItunesPodcastRepository,
        private val rssRepository: RssRepository,
        private val podcastEpisodeRepository: PodcastEpisodeRepository,
        private val podcastRepository: PodcastRepository
) : PlaybackStateAwareViewModel(downloadManager, podcastEpisodeRepository, errorMapper, viewItemToDatabaseItemMapper) {

    init {
        initMediaCallback(context)
    }

    private val _loadPodcast = MutableLiveData<PodcastDetails>()
    val loadPodcast: LiveData<PodcastDetails> = _loadPodcast

    private val _episodes = MutableLiveData<List<PodcastEpisodeViewItem>>()
    val episodes: LiveData<List<PodcastEpisodeViewItem>> = _episodes

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _webSite = MutableLiveData<String>()
    val webSite: LiveData<String> = _webSite

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

    fun loadFeed(podcast: PodcastDetails, podcastId: String) {
        GlobalScope.launch {
            val rss = rssRepository.loadRss(podcast.feedUrl)

            val favourites = podcastEpisodeRepository.getFavourites()?.map { item -> item.id }
            val downloads = podcastEpisodeRepository.getDownloads()?.map { it.id to it }?.toMap()
//            val downloadIds = downloads?.map { item -> item.id }
            episodeItems = rss.items().mapIndexed { index, item ->
                remoteToViewItemMapper.map(index, item, rss.link(), podcastId).apply {
                    isFavourite = favourites?.contains(id) ?: false
                    downloadState = if (downloads?.contains(id) == true) DownloadState(isDownloaded = downloads[id]?.state == STATE_DOWNLOADED) else null
                    isPlaying = mediaConnection.playbackState?.isPlaying == true &&
                            mediaConnection.playbackMetadata?.description?.mediaId == link
                }
            }

            listenForActiveDownloads(downloadManager, podcastId)

            _episodes.postValue(episodeItems)
            _description.postValue(rss.description())
            _webSite.postValue(rss.webSite())
        }
    }

    fun isSubscribed(id: String) {
        GlobalScope.async {
            _isSubscribed.postValue(podcastRepository.isSubscribed(id))
        }
    }

    fun subscribe(podcast: PodcastAdapterEntry, details: PodcastDetails, explicitlyInvoked: Boolean = false) {
        GlobalScope.async {
            val subscribedPodcast = SubscribedPodcast(podcast.id(), podcast.title(), podcast.logo(), details.feedUrl,
                    details.primaryGenreName, details.genreIds?.joinToString(), details.authorId, details.authorName)

            val isSubscribed = podcastRepository.toggleSubscribe(subscribedPodcast)
            _isSubscribed.postValue(isSubscribed)

            if (explicitlyInvoked) {
                _showUndo.postValue(isSubscribed)
            }
        }
    }

}