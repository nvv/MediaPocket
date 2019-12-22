package com.mediapocket.android

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.Storage
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.splash_screen.*
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
class SplashScreenActivity : DaggerAppCompatActivity() {

    @set:Inject
    lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DependencyLocator.initInstance(this)
        AndroidInjection.inject(this)

        setContentView(R.layout.splash_screen)

        if (!storage.showAnimationLogo) {
            logo.setImageResource(R.drawable.logo_finished)
            logo.postDelayed({
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }, 500)
        } else {
            val animated = AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.logo_animated)
            animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    storage.showAnimationLogo = false
                    startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                    finish()
                }
            })

            logo.setImageDrawable(animated)
            animated?.start()
        }

    }

}