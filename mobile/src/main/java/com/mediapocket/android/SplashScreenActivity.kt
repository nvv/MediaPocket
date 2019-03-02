package com.mediapocket.android

import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import kotlinx.android.synthetic.main.splash_screen.*

/**
 * @author Vlad Namashko
 */
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val animated = AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.logo_animated)
                    animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                            finish()
                        }
                    })

        logo.setImageDrawable(animated)
        animated?.start()
    }

}