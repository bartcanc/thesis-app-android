package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)
        val gifDrawable = gifImageView.drawable as GifDrawable

        gifDrawable.loopCount = 1

        gifDrawable.addAnimationListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
