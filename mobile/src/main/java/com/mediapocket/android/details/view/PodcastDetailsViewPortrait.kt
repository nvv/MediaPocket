package com.mediapocket.android.details.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.ChangeStatusBarColorEvent
import com.mediapocket.android.events.PopBackStackEvent
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.model.PodcastDetails
import com.mediapocket.android.view.DotPager
import kotlinx.android.synthetic.main.podcast_details_view_phone.view.*
import org.jetbrains.anko.backgroundColor

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewPortrait(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PodcastDetailsView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private var collapsingToolbar: CollapsingToolbarLayout? = null
    private var toolbar: Toolbar

    private lateinit var dotPager: DotPager

    init {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            RxBus.default.postEvent(PopBackStackEvent())
        }
        toolbar.inflateMenu(R.menu.podcast_details_menu)

        val pager: androidx.viewpager.widget.ViewPager? = findViewById(R.id.pager)
        pager?.let {
            dotPager = findViewById(R.id.dot_pager)
            dotPager.initView(2, 0)

            it.adapter = object : androidx.viewpager.widget.PagerAdapter() {
                override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
                    return arg0 == arg1
                }

                override fun getCount(): Int {
                    return 2
                }

                override fun instantiateItem(collection: ViewGroup, position: Int): Any {
                    var resId = if (position == 0) R.id.background_frame else R.id.description
                    return it.findViewById(resId)
                }
            }

            it.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    dotPager.setCurrentItem(position)
                }

            })
        }
    }

    override fun hasOptionsMenu() = true

    override fun getMenu(): Menu? = toolbar.menu

    override fun fragmentCreated(podcast: PodcastAdapterEntry?) {
        podcast?.let {
            loadLogo(it.logo())
        }

        collapsingToolbar?.title = podcast?.title()
    }

    override fun fullDataFetched(details: PodcastDetails) {
        details.primaryGenreName?.let {
            genre.text = "#$it"
        }
        genre.visibility = if (details.primaryGenreName != null) VISIBLE else GONE
    }

    override fun logoLoaded(bitmap: Bitmap, palette: androidx.palette.graphics.Palette) {
        val color = palette.getDarkVibrantColor(R.attr.colorPrimary) or 0xFF000000.toInt()
        description.setBackgroundColor(color)

        collapsingToolbar?.setContentScrimColor(color)
        subscribe?.backgroundTintList = ColorStateList.valueOf(color)

        genre.backgroundColor = color
        RxBus.default.postEvent(ChangeStatusBarColorEvent(color))
    }

    override fun getLayout(): Int {
        return R.layout.podcast_details_view_phone
    }

}