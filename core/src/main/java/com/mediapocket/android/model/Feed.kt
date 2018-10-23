package com.mediapocket.android.model

import android.mediapocket.com.core.R
import com.google.gson.annotations.SerializedName
import com.mediapocket.android.core.DependencyLocator

/**
 * @author Vlad Namashko
 */
data class Result(val feed: Feed) : Cacheable(), PodcastDiscoverResult {

    override fun cacheOnDisk() = true

    override fun keepFor() = 180 * 60 * 1000 // 3 hours

    override fun title() : String = DependencyLocator.getInstance().context.getString(R.string.top_podcasts)
}

data class Feed(val title: String,
                @SerializedName("results") val items: List<TopPodcast>)

data class TopPodcast(val id: String,
                      @SerializedName("artistName") val author: String,
                      val releaseDate: String,
                      val name: String,
                      @SerializedName("artworkUrl100") val logo: String,
                      val genreIds: List<Genre>)


//"artistName":"Stitcher",
//"id":"1348032695",
//"releaseDate":"2018-02-28",
//"name":"Dear Franklin Jones",
//"kind":"podcast",
//"copyright":"© All rights reserved.",
//"artworkUrl100":"http://is3.mzstatic.com/image/thumb/Music128/v4/85/47/6f/85476f74-a2ed-9e3c-9fd5-0a7c4ae1bb9b/source/200x200bb.png",
//"genres":[
//{
//    "genreId":"1462",
//    "name":"History",
//    "url":"https://itunes.apple.com/us/genre/id1462"
//},
//{
//    "genreId":"26",
//    "name":"Podcasts",
//    "url":"https://itunes.apple.com/us/genre/id26"
//},
//{
//    "genreId":"1324",
//    "name":"Society \u0026 Culture",
//    "url":"https://itunes.apple.com/us/genre/id1324"
//}
//],
//"url":"https://itunes.apple.com/us/podcast/dear-franklin-jones/id1348032695?mt=2"
//},