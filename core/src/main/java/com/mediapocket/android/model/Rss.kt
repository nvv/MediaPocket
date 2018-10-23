package com.mediapocket.android.model

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import com.mediapocket.android.utils.XmlNode
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import java.util.logging.SimpleFormatter


/**
 * @author Vlad Namashko
 */
class Rss(rss: String?, overrideLink: String? = null) : Cacheable() {

    private val title: String
    private val description: String
    private val managingEditor: String
    private val copyright: String
    private val link: String
    private val webSite: String
    private val language: String
    private val image: String
    private val items: MutableList<Item> = mutableListOf()

    init {
        val root = XmlNode()
        root.parse(rss)
        val channel = root.getChild("channel")

        title = channel.getChild("title").value
        description = channel.getChild("description").value
        managingEditor = channel.getChild("managingEditor").value
        copyright = channel.getChild("copyright").value
        link = overrideLink ?: channel.getChild("link").value

        val linkVal = channel.getChild("link").value
        webSite = linkVal ?: channel.getChild("docs").value ?: link
        language = channel.getChild("language").value

        val url= channel.getChild("image").getAttribute("href")
        image = url ?: channel.getChild("image").getChild("url").value

        channel.children.filter { node -> node.name == "item" }.forEach {
            item -> items.add(Item(title, item.getChild("title").value,
                item.getChild("description").value, item.getChild("pubDate").value,
                item.getChild("enclosure").getAttribute("url"),
                if (item.hasChild("enclosure") && item.getChild("enclosure").getAttribute("length") != null) item.getChild("enclosure").getAttribute("length").toLong() else -1,
                if (item.hasChild("image")) item.getChild("image").getAttribute("href") else image)) }
    }

    fun description() = description

    fun items() = items

    fun link() = link

    fun webSite() = webSite

    override fun cacheOnDisk() = true

    override fun keepFor() = 720 * 60 * 1000 // half day
}

data class Item(val podcastTitle: String, val title: String, val description: String,
                val pubDate: String, val link: String?, val length: Long, val imageUrl: String) {

    private var pubDateFormatter: String? = null

    fun dateFormatted(): String {
        pubDateFormatter?.let {
            return it
        }

        val pubDateObj = Date(pubDate)
        val calendar = Calendar.getInstance()
        calendar.time = pubDateObj

        if (calendar.get(Calendar.YEAR) == currentYear) {
            pubDateFormatter = monthFormatter.format(pubDateObj) + "\n" + calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            pubDateFormatter = dateFormatter.format(pubDateObj) + "\n" + calendar.get(Calendar.YEAR)
        }

        return pubDateFormatter!!
    }

    private fun getMediaExtras(): Bundle {
        val bundle = Bundle()

        bundle.putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, podcastTitle)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_DATE, dateFormatted())
        bundle.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, link)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ART, imageUrl)

        return bundle
    }

    fun getMediaMetadataCompat() : MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, link)
                .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, podcastTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateFormatted())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, link)
//                .putString(MediaMetadataCompat.METADATA_KEY_ART, imageUrl)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, imageUrl)
                .build()
    }

    fun getMediaDescription(): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
                .setTitle(title)
                .setIconUri(Uri.parse(imageUrl))
                .setMediaId(link)
                .setExtras(getMediaExtras())
                .build()
    }

    companion object {
        private val monthFormatter = SimpleDateFormat("MMM", Locale.ENGLISH)
        private val dateFormatter = SimpleDateFormat("dd/MM", Locale.ENGLISH)
        private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    }

}
