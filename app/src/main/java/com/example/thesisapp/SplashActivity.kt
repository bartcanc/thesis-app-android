package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)
        val gifDrawable = gifImageView.drawable as GifDrawable
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        gifDrawable.loopCount = 1

        gifDrawable.addAnimationListener {
            val rememberMe = sharedPref.getBoolean("remember_me", false)
            if(rememberMe){
                startActivity(Intent(this, MainActivity::class.java))
            }
            else{
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }
}
