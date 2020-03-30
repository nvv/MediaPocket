package com.mediapocket.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.details.viewitem.PodcastEpisodeViewItem
import com.mediapocket.android.details.viewitem.isDownloading
import com.mediapocket.android.details.viewitem.isError

class PodcastEpisodeToolbar(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private val progress: CircularProgressBar
    private val favourite: ImageView
    private val share: ImageView
    private val more: ImageView
    private val status: ImageView

    private val error: TextView


    init {
        LayoutInflater.from(context).inflate(R.layout.podcast_episodes_toolbar, this)
        progress = findViewById(R.id.downloadProgress)
        favourite = findViewById(R.id.episodeFavorite)
        share = findViewById(R.id.episodeShare)
        more = findViewById(R.id.episodeContextMenu)
        status = findViewById(R.id.downloadStatus)

        error = findViewById(R.id.error)
    }

    fun applyTint(accentColor: Int) {
        progress.foregroundStrokeColor = accentColor
        favourite.setColorFilter(accentColor)
        share.setColorFilter(accentColor)
        more.setColorFilter(accentColor)
        status.setColorFilter(accentColor)
    }

    fun bind(item: PodcastEpisodeViewItem, listener: EpisodeItemListener? = null) {

        share.setOnClickListener {
            listener?.share(item)
        }

        error.setOnClickListener {
            listener?.statusClicked(item)
        }

        favourite.setOnClickListener {
            listener?.favouriteClicked(item)
        }

        status.setOnClickListener {
            listener?.statusClicked(item)
        }

        val stateSet = intArrayOf(android.R.attr.state_checked * if (item.isFavourite) 1 else -1)
        favourite.setImageState(stateSet, true)

        if (item.isError) {
            error.visibility = View.VISIBLE
            error.text = item.downloadState?.error
            status.setImageResource(R.drawable.ic_download)
        } else {
            error.visibility = View.GONE
            status.setImageResource(item.getStatusIcon())
        }

        if (item.isDownloading) {
            progress.visibility = View.VISIBLE
            progress.progress = item.downloadState?.progress?.toFloat() ?: 0F
        } else {
            progress.visibility = View.GONE
        }

    }

}