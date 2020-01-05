package com.mediapocket.android.journeys.details.vm

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mediapocket.android.MediaSessionConnection
import com.mediapocket.android.core.download.manager.PodcastDownloadManager
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.dao.model.PodcastEpisodeItem.Companion.STATE_DOWNLOADED
import com.mediapocket.android.dao.model.SubscribedPodcast
import com.mediapocket.android.extensions.isPlaying
import com.mediapocket.android.journeys.details.viewitem.DownloadState
import com.mediapocket.android.journeys.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.repository.ItunesPodcastRepository
import com.mediapocket.android.repository.PodcastEpisodeRepository
import com.mediapocket.android.repository.PodcastRepository
import com.mediapocket.android.repository.RssRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewModel @Inject constructor(
        private val context: Context,
        private val downloadManager: PodcastDownloadManager,
        private val itunesPodcastRepository: ItunesPodcastRepository,
        private val rssRepository: RssRepository,
        private val podcastEpisodeRepository: PodcastEpisodeRepository,
        private val podcastRepository: PodcastRepository
) : ViewModel() {

    private lateinit var mediaConnection: MediaSessionConnection
    private val mediaCallback: MediaControllerCompat.Callback

    // TODO: tmp
    private var episodeItems : List<PodcastEpisodeViewItem>? = null

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

    private val _episodesChanged = MutableLiveData<Set<Int>>()
    val episodesChanged: LiveData<Set<Int>> = _episodesChanged

    init {
        mediaCallback = object : MediaControllerCompat.Callback() {
            private var playingEpisodeId: String? = null

            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                mediaConnection.mediaController.metadata?.description?.mediaId?.let { it ->
                    itemPlaybackChanged(it, state.isPlaying)
                }

            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                metadata?.description?.mediaId?.let {
                    itemPlaybackChanged(it, mediaConnection.mediaController.playbackState.isPlaying)
                }
            }

            private fun itemPlaybackChanged(itemLink: String, playing: Boolean) {
                val id = PodcastEpisodeItem.convertLinkToId(itemLink)

                val changed = mutableSetOf<Int>()
                if (playingEpisodeId != id) {
                    playingEpisodeId?.let {
                        episodeItems?.find { item -> item.id == it }?.apply {
                            isPlaying = false
                            changed.add(position)
                        }
                    }
                }

                episodeItems?.find { item -> item.id == id }?.apply {
                    if (isPlaying != playing) {
                        changed.add(position)
                    }
                    isPlaying = playing
                    playingEpisodeId = id
                }

                if (changed.isNotEmpty()) {
                    _episodesChanged.postValue(changed)
                }
            }
        }

        mediaConnection = MediaSessionConnection.getInstance(context).apply {
            registerMediaControllerCallback(mediaCallback)
        }
    }

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
                PodcastEpisodeViewItem(index, item, rss.link(), podcastId).apply {
                    isFavourite = favourites?.contains(id) ?: false
                    downloadState = if (downloads?.contains(id) == true) DownloadState(isDownloaded = downloads[id]?.state == STATE_DOWNLOADED) else null
                }
            }

            _episodes.postValue(episodeItems)
            _description.postValue(rss.description())
            _webSite.postValue(rss.webSite())
        }
    }

    fun favouriteEpisode(episode: PodcastEpisodeViewItem) {
        GlobalScope.launch {
            episode.isFavourite = podcastEpisodeRepository.toggleFavourite(episode.podcastId, episode.item)
            _episodesChanged.postValue(setOf(episode.position))
        }
    }

    fun downloadItem(episode: PodcastEpisodeViewItem) {
        val process = downloadManager.download(episode.podcastId, episode.item)

        GlobalScope.launch {
            process?.consumeEach { item ->
                if (episode.downloadState == null) {
                    episode.downloadState = DownloadState()
                }

                episode.downloadState?.state = item.state
                episode.downloadState?.progress = item.progress
                // TODO
                episode.downloadState?.isDownloaded = item.progress == 100
                _episodesChanged.postValue(setOf(episode.position))
            }

        }
    }

    fun pauseDownload(episode: PodcastEpisodeViewItem) {
        downloadManager.pauseDownload(episode.item)
    }

    fun resumeDownload(episode: PodcastEpisodeViewItem) {
        downloadManager.resumeDownload(episode.item)
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

    override fun onCleared() {
        super.onCleared()
        mediaConnection.unregisterMediaControllerCallback(mediaCallback)
    }

}