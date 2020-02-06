package com.mediapocket.android.details.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.Menu
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.view.AnimatedImageView

/**
 * @author Vlad Namashko
 */
class PodcastDetailsViewLandscape(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PodcastDetailsView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context?) : this(context, null, -1)

    private var animatedBackground: AnimatedImageView = findViewById(R.id.animated_background)
    private var animatedPanel: ViewGroup = findViewById(R.id.left_panel)

    init {

    }

    override fun fragmentCreated(podcast: PodcastAdapterEntry?) {
        podcast?.let {
            loadLogo(it.logo())
        }
    }

    override fun getLayout(): Int {
        return R.layout.podcast_details_view_landscape
    }

    override fun logoLoaded(bitmap: Bitmap, palette: androidx.palette.graphics.Palette) {
        animatedPanel.setBackgroundColor(palette.getDarkVibrantColor(R.attr.colorPrimary))
//            if (dataLoaded) {
                animatedBackground.postDelayed({
                    animatedBackground.setImageDrawable(BitmapDrawable(bitmap))
                    animatedBackground.startAnimation()
                    val animator = ObjectAnimator.ofFloat(animatedBackground, "alpha", 0f, 1f).setDuration(1000)
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            animatedBackground.startAnimation()
                        }
                    })
                    animator.start()
//                                    panel.setBackgroundColor(resources.getColor(R.color.white_semi_transparent))
                }, 2000)
//            }

    }

    override fun hasOptionsMenu() = false

    override fun getMenu(): Menu? = null

}