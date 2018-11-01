package com.mediapocket.android.model

import android.os.Parcel
import android.os.Parcelable
import com.mediapocket.android.dao.model.SubscribedPodcast

/**
 * @author Vlad Namashko
 */
class PodcastAdapterEntry() : Parcelable {

    private lateinit var id: String

    private lateinit var title: String

    private lateinit var logo: String

    private var feedUrl: String? = null

    private var primaryGenreName: String? = null

    private var genreIds: List<Int>? = null

    private var artistId: Int? = null

    private var artistName: String? = null

    constructor(podcast: TopPodcast) : this() {
        title = podcast.name
        logo = podcast.logo
        id = podcast.id
    }

    constructor(podcast: Podcast) : this() {
        title = podcast.artistName
        logo = podcast.artworkUrl600
        id = podcast.artistId.toString()
        feedUrl = podcast.feedUrl
        genreIds = podcast.genreIds
        primaryGenreName = podcast.primaryGenreName
        artistId = podcast.artistId
        artistName = podcast.artistName
    }

    constructor(podcast: GenreResult.PodcastEntry) : this() {
        title = podcast.name
        logo = podcast.logo
        id = podcast.id
    }

    constructor(podcast: SubscribedPodcast) : this() {
        title = podcast.title
        logo = podcast.logo
        id = podcast.id
        feedUrl = podcast.feedUrl
        primaryGenreName = podcast.primaryGenre
        artistId = podcast.artistId?.toInt()
        artistName = podcast.artistName
    }

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        logo = parcel.readString()
        id = parcel.readString()
        feedUrl = parcel.readString()
    }

    fun title() = title
    fun logo() = logo
    fun id() = id
    fun feedUrl(): String? = feedUrl
    fun primaryGenreName() = primaryGenreName
    fun genreIds() = genreIds
    fun artistId() = artistId
    fun artistName() = artistName

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(logo)
        parcel.writeString(id)
        parcel.writeString(feedUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PodcastAdapterEntry> {
        override fun createFromParcel(parcel: Parcel): PodcastAdapterEntry {
            return PodcastAdapterEntry(parcel)
        }

        override fun newArray(size: Int): Array<PodcastAdapterEntry?> {
            return arrayOfNulls(size)
        }

        fun convertToAdapterEntries(res: PodcastDiscoverResult?) : List<PodcastAdapterEntry> {
            return when (res) {
                is Result -> convert(res)
                is SearchResult -> convert(res)
                is GenreResult -> convert(res)
                is SubscriptionsLookupResult -> convert(res)
                else -> listOf()
            }
        }

        fun convert(res: Result?): List<PodcastAdapterEntry> {
            val items = mutableListOf<PodcastAdapterEntry>()

            res?.let {
                it.feed.items.forEach { item -> items.add(PodcastAdapterEntry(item))}
            }

            return items
        }

        fun convert(res: SearchResult?): List<PodcastAdapterEntry> {
            val items = mutableListOf<PodcastAdapterEntry>()

            res?.let {
                it.results.forEach { item -> items.add(PodcastAdapterEntry(item))}
            }

            return items
        }

        fun convert(res: GenreResult?): List<PodcastAdapterEntry> {
            val items = mutableListOf<PodcastAdapterEntry>()

            res?.let {
                it.entries.forEach { item -> items.add(PodcastAdapterEntry(item))}
            }

            return items
        }

        fun convert(res: SubscriptionsLookupResult?): List<PodcastAdapterEntry> {
            val items = mutableListOf<PodcastAdapterEntry>()

            res?.let {
                it.items.forEach { item -> items.add(PodcastAdapterEntry(item))}
            }

            return items
        }
    }
}