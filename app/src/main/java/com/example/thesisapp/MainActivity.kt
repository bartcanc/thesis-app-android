package com.example.thesisapp

import ApiClient
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity: BaseActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedLanguage = sharedPref.getString("selected_language", "pl")
        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        setContentView(R.layout.activity_main)

        val userInfoContainer = findViewById<LinearLayout>(R.id.userInfoContainer)
        val llMyTrainings = findViewById<LinearLayout>(R.id.llMyTrainings)
        val llForum = findViewById<LinearLayout>(R.id.llForum)
        val llBadges = findViewById<LinearLayout>(R.id.llBadges)
        val llBMICalculator = findViewById<LinearLayout>(R.id.llBMICalculator)

        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)

        val selectedTheme = sharedPref.getString("theme", "sea") // domyślnie Sea Breeze

        if (selectedTheme == "post") {
            userInfoContainer.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
            llMyTrainings.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
            llForum.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
            llBadges.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
            llBMICalculator.setBackgroundResource(R.drawable.rounded_button_background_post_modern)

            bottomNav.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            userInfoContainer.setBackgroundResource(R.drawable.rounded_button_background_main)
            llMyTrainings.setBackgroundResource(R.drawable.rounded_button_background_main)
            llForum.setBackgroundResource(R.drawable.rounded_button_background_main)
            llBadges.setBackgroundResource(R.drawable.rounded_button_background_main)
            llBMICalculator.setBackgroundResource(R.drawable.rounded_button_background_main)

            bottomNav.setBackgroundResource(R.drawable.gradient_background)
        }

        val btnBand = findViewById<LinearLayout>(R.id.btnBand)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnMyTrainings = findViewById<LinearLayout>(R.id.llMyTrainings)
        val btnBMICalories = findViewById<LinearLayout>(R.id.llBMICalculator)
        val btnForum = findViewById<LinearLayout>(R.id.llForum)
        val btnBadges = findViewById<LinearLayout>(R.id.llBadges)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        val username = sharedPref.getString("username", "User")
        tvWelcome.text = "${this.getString(R.string.welcome_testuser)}$username!"

        if(userId != null && sessionId != null) {
            fetchUserAvatar(this, userId!!, sessionId!!)
        }
        btnMyTrainings.setOnClickListener{
            val intent = Intent(this, TrainingListActivity::class.java)
            startActivity(intent)
        }

        btnBMICalories.setOnClickListener{
            val intent = Intent(this, BMICaloriesActivity::class.java)
            startActivity(intent)
        }

        // Przyciski nawigacji
        btnSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBand.setOnClickListener{
            val intent = Intent(this, BandActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnForum.setOnClickListener {
            Toast.makeText(this, "This feature will be added in the future.", Toast.LENGTH_SHORT).show()
        }

        btnBadges.setOnClickListener {
            Toast.makeText(this, "This feature will be added in the future.", Toast.LENGTH_SHORT).show()
        }
    }

    fun fetchUserAvatar(context: Context, userId: String, sessionId: String) {
        val apiClient = ApiClient(context)
        val apiService = apiClient.getApiService8000()

        apiService.getUserAvatar(userId, sessionId).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                Log.d("fetchUserAvatar", "Full response: ${response.raw()}")
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val responseData = responseBody.string()
                            Log.d("fetchUserAvatar", "Response body: $responseData")
                            val jsonResponse = JSONObject(responseData)
                            var avatarUrl = jsonResponse.optString("avatar_link", "")

                            if (avatarUrl.isNotEmpty()) {
                                if (avatarUrl.contains("localhost")) {
                                    avatarUrl = avatarUrl.replace("localhost", "192.168.108.97")
                                    Log.d("fetchUserAvatar", "Updated Avatar URL: $avatarUrl")
                                }
                                downloadAndDisplayImage(context, avatarUrl)
                            } else {
                                Log.e("fetchUserAvatar", "Avatar URL is empty")
                            }
                        } catch (e: Exception) {
                            Log.e("fetchUserAvatar", "Error parsing response: ${e.message}")
                        }
                    } ?: Log.e("fetchUserAvatar", "Empty response body")
                } else {
                    Log.e("fetchUserAvatar", "Error response: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Failed to fetch avatar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("fetchUserAvatar", "Request failed: ${t.message}")
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadAndDisplayImage(context: Context, imageUrl: String) {
        Thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    (context as? Activity)?.runOnUiThread {
                        context.findViewById<ImageView>(R.id.imgProfilePicture).setImageBitmap(bitmap)
                    }
                } else {
                    Log.e("downloadAndDisplayImage", "Failed to download image: ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                Log.e("downloadAndDisplayImage", "Error downloading image: ${e.message}")
            }
        }.start()
    }
}