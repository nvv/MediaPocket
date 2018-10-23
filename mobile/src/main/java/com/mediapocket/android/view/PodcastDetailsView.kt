package com.mediapocket.android.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.ShimmerFrameLayout
import com.mediapocket.android.R
import com.mediapocket.android.adapters.PodcastItemsAdapter
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.Rss
import com.mediapocket.android.core.AppDatabase
import android.arch.persistence.room.Room
import android.support.v4.view.ViewCompat
import android.view.Menu
import com.mediapocket.android.model.PodcastDetails


/**
 * @author Vlad Namashko
 */
abstract class PodcastDetailsView (context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    protected var items: RecyclerView
    protected var shimmerContainer: ShimmerFrameLayout
    protected var description: TextView
    var logo: ImageView

    init {
        @Suppress("LeakingThis")
        LayoutInflater.from(context).inflate(getLayout(), this)

        items = findViewById(R.id.items)
        val layoutManager = LinearLayoutManager(context)
        items.layoutManager = layoutManager

        logo = findViewById(R.id.background)

        description = findViewById(R.id.description)

        shimmerContainer = findViewById(R.id.shimmer_view_container)
        shimmerContainer.startShimmerAnimation()
    }

    fun feedLoaded(rss: Rss) {
        shimmerContainer.stopShimmerAnimation()
        shimmerContainer.visibility = View.GONE

        description.text = Html.fromHtml(rss.description())

        items.adapter = PodcastItemsAdapter(rss.items(), rss.link())
    }

    fun loadLogo(logoUrl: String) {
        logo.context?.let {
            Glide.with(it).asBitmap().load(logoUrl).into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    logo.setImageBitmap(bitmap)
                    setImageTint(TINT_HARD)

                    Palette.from(bitmap).generate { palette ->
                        logoLoaded(bitmap, palette)
                    }

                }
            })
        }
    }

    open fun fullDataFetched(details: PodcastDetails) {

    }

    private fun setImageTint(tint: ColorDrawable) {
        if (Build.VERSION.SDK_INT >= 23) {
            logo.foreground = tint
        } else {
            logo.colorFilter = PorterDuffColorFilter(tint.color, PorterDuff.Mode.SRC_OVER)
        }
    }

    companion object {
        private val TINT_HARD = ColorDrawable(-0x80000000) // 50% transparency
//        private val TINT_SOFT = ColorDrawable(0x1A000000) // 10% transparency
    }

    abstract fun hasOptionsMenu(): Boolean

    abstract fun getMenu(): Menu?

    abstract fun fragmentCreated(podcast: PodcastAdapterEntry?)

    abstract fun getLayout(): Int

    abstract fun logoLoaded(bitmap: Bitmap, palette: Palette)
}