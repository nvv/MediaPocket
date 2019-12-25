package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.google.gson.annotations.SerializedName
import com.mediapocket.android.core.DependencyLocator

/**
 * @author Vlad Namashko
 */
data class SearchResult(val resultCount: Int, val results: List<Podcast>) : Cacheable(), PodcastDiscoverResult {

    override fun keepFor() = 30 * 60 * 1000 // 30 mins

    override fun title(): String = DependencyLocator.getInstance().context.getString(android.R.string.search_go)
}

data class Podcast(val artistId: Int,
                   val collectionId: Int,
                   val artistName: String,
                   val collectionName: String,
                   val feedUrl: String,
                   val artworkUrl600: String,
                   val releaseDate: String,
                   val primaryGenreName: String,
                   val genreIds: List<Int>,
                   val genres: List<String>)

//"wrapperType":"track",
//"kind":"podcast",
//"artistId":121676617,
//"collectionId":135067274,
//"trackId":135067274,
//"artistName":"BBC World Service",
//"collectionName":"Global News Podcast",
//"trackName":"Global News Podcast",
//"collectionCensoredName":"Global News Podcast",
//"trackCensoredName":"Global News Podcast",
//"artistViewUrl":"https://itunes.apple.com/us/artist/bbc/121676617?mt=2&uo=4",
//"collectionViewUrl":"https://itunes.apple.com/us/podcast/global-news-podcast/id135067274?mt=2&uo=4",
//"feedUrl":"https://podcasts.files.bbci.co.uk/p02nq0gn.rss",
//"trackViewUrl":"https://itunes.apple.com/us/podcast/global-news-podcast/id135067274?mt=2&uo=4",
//"artworkUrl30":"http://is2.mzstatic.com/image/thumb/Music118/v4/f3/da/82/f3da82f8-43bf-6061-005b-00a5bedd27a7/source/30x30bb.jpg",
//"artworkUrl60":"http://is2.mzstatic.com/image/thumb/Music118/v4/f3/da/82/f3da82f8-43bf-6061-005b-00a5bedd27a7/source/60x60bb.jpg",
//"artworkUrl100":"http://is2.mzstatic.com/image/thumb/Music118/v4/f3/da/82/f3da82f8-43bf-6061-005b-00a5bedd27a7/source/100x100bb.jpg",
//"collectionPrice":0.00,
//"trackPrice":0.00,
//"trackRentalPrice":0,
//"collectionHdPrice":0,
//"trackHdPrice":0,
//"trackHdRentalPrice":0,
//"releaseDate":"2018-03-23T23:57:00Z",
//"collectionExplicitness":"cleaned",
//"trackExplicitness":"cleaned",
//"trackCount":53,
//"country":"USA",
//"currency":"USD",
//"primaryGenreName":"News & Politics",
//"contentAdvisoryRating":"Clean",
//"artworkUrl600":"http://is2.mzstatic.com/image/thumb/Music118/v4/f3/da/82/f3da82f8-43bf-6061-005b-00a5bedd27a7/source/600x600bb.jpg",
//"genreIds":[
//"1311",
//"26"
//],
//"genres":[
//"News & Politics",
//"Podcasts"
//]
//},