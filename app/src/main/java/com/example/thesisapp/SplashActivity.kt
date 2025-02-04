package com.example.thesisapp

import ApiClient
import LoginRequest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.ResponseBody
import org.json.JSONObject
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ukryj pasek akcji, jeśli jeszcze widoczny
        supportActionBar?.hide()

// Layout fullscreen z zachowaniem paska systemowego w postaci nakładki
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

// Dodatkowo przezroczysty status bar:
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_splash)

        val rootLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
        // lub jakikolwiek inny "główny" layout z Twojego XML-a

        // Przykładowy odczyt z SharedPreferences
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea") // domyślnie "sea"

        // Jeżeli to jest "post modern", zmieniamy background:
        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            // Sea Breeze (domyślnie)
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)
        val gifDrawable = gifImageView.drawable as GifDrawable

        apiClient = ApiClient(this)

        gifDrawable.loopCount = 1

        gifDrawable.addAnimationListener {
            val rememberMe = sharedPref.getBoolean("remember_me", false)
            if (rememberMe) {
                val username = sharedPref.getString("username", null)
                val password = sharedPref.getString("password", null)
                if (username != null && password != null) {
                    performLogin(username, password)
                } else {
                    navigateToLogin()
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        val apiService = apiClient.getApiService8000()

        apiService.login(loginRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val sessionId = response.headers()["session_id"]
                    sessionId?.let {
                        sharedPref.edit().putString("session_id", it).apply()
                        Log.d("performLogin", "session-id saved in SharedPreferences: $it")
                    }
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                } else {
                    navigateToLogin()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                navigateToLogin()
            }
        })
    }

    private fun navigateToLogin() {
        with(sharedPref.edit()) {
            remove("username")
            remove("password")
            remove("remember_me")
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
