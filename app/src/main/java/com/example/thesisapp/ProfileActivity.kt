package com.example.thesisapp

import ApiClient
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL

class ProfileActivity: BaseActivity(){
    private lateinit var tvUsername: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvBMR: TextView
    private lateinit var tvTDEE: TextView

    private var gender = ""
    private var age = 0
    private var weight = 0
    private var height = 0
    private var bmr = 0
    private var tdee = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)

        val selectedTheme = sharedPref.getString("theme", "sea")

        if (selectedTheme == "post") {
            bottomNav.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            bottomNav.setBackgroundResource(R.drawable.gradient_background)
        }

        val apiClient = ApiClient(this)
        apiService = apiClient.getApiService8000()

        tvUsername = findViewById(R.id.tvUsername)

        tvGender = findViewById(R.id.tvGender)
        tvAge = findViewById(R.id.tvAge)
        tvWeight = findViewById(R.id.tvWeight)
        tvHeight = findViewById(R.id.tvHeight)
        tvBMR = findViewById(R.id.tvBMR)
        tvTDEE = findViewById(R.id.tvTDEE)

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val btnHome = findViewById<LinearLayout>(R.id.btnHome)
        val btnBand = findViewById<LinearLayout>(R.id.btnBand)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)

        val username = sharedPref.getString("username", "Unknown User")
        tvUsername.text = username

        fetchUserMetrics(userId, sessionId)
        fetchUserAvatar(userId, sessionId)

        btnLogout.setOnClickListener{
            performLogout(sharedPref)
            webSocketHelper.stopConnection()
        }

        // Przyciski nawigacji
        btnHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBand.setOnClickListener{
            val intent = Intent(this, BandActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun fetchUserAvatar(userId: String?, sessionId: String?) {
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        if (userId != null && sessionId != null) {
            apiService.getUserAvatar(userId, sessionId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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
                                    downloadAndDisplayImage(avatarUrl)
                                } else {
                                    Log.e("fetchUserAvatar", "Avatar URL is empty")
                                }
                            } catch (e: Exception) {
                                Log.e("fetchUserAvatar", "Error parsing response: ${e.message}")
                            }
                        } ?: Log.e("fetchUserAvatar", "Empty response body")
                    } else {
                        Log.e("fetchUserAvatar", "Error response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ProfileActivity, "Failed to fetch avatar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("fetchUserAvatar", "Request failed: ${t.message}")
                    Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun downloadAndDisplayImage(imageUrl: String) {
        Thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // Wyświetlenie obrazu w UI
                    runOnUiThread {
                        findViewById<ImageView>(R.id.imgProfilePicture).setImageBitmap(bitmap)
                    }
                } else {
                    Log.e("downloadAndDisplayImage", "Failed to download image: ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                Log.e("downloadAndDisplayImage", "Error downloading image: ${e.message}")
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        fetchUserAvatar(userId, sessionId)
        tvGender.text = gender
        tvAge.text = age.toString()
        tvWeight.text = weight.toString()
        tvHeight.text = height.toString()
        tvBMR.text = bmr.toString()
        tvTDEE.text = tdee.toString()

        Log.d("onResume", "Gender: $gender, Age: $age, Weight: $weight, Height: $height")
    }


    private fun fetchUserMetrics(userId: String?, sessionId: String?) {
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        if (userId != null && sessionId != null) {
            apiService.getUserMetrics(userId, sessionId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            try {
                                val responseData = responseBody.string()
                                Log.d("fetchUserMetrics", "Response data: $responseData")

                                // Parsowanie JSON i pobranie danych z klucza `user`
                                val userObject = JSONObject(responseData).getJSONObject("user")

                                runOnUiThread {
                                    // Wyciągnięcie poszczególnych wartości
                                    gender = userObject.optString("gender", "unknown")
                                    age = userObject.optInt("age", 0)
                                    weight = userObject.optInt("weight", 0)
                                    height = userObject.optInt("height", 0)
                                    val bmr = userObject.optInt("bmr", 0)
                                    val tdee = userObject.optInt("tdee", 0)

                                    // Aktualizacja widoków
                                    tvGender.text = gender
                                    tvAge.text = age.toString()
                                    tvWeight.text = weight.toString()
                                    tvHeight.text = height.toString()
                                    tvBMR.text = bmr.toString()
                                    tvTDEE.text = tdee.toString()

                                    Log.d(
                                        "ValueUpdate",
                                        "Gender: $gender, Age: $age, Weight: $weight, Height: $height, BMR: $bmr, TDEE: $tdee"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("fetchUserMetrics", "Error parsing response: ${e.message}")
                            }
                        } ?: Log.e("fetchUserMetrics", "Empty response body")
                    } else {
                        Log.e("fetchUserMetrics", "Error response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ProfileActivity, "Failed to fetch user metrics", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("fetchUserMetrics", "Request failed: ${t.message}")
                    Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun performLogout(sharedPref: SharedPreferences) {
        apiService.logout().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("performLogout", "Response code: ${response.code()}")
                Log.d("performLogout", "Response message: ${response.message()}")

                if (response.isSuccessful && response.code() == 204) {
                    Log.d("performLogout", "Wylogowanie powiodło się.")
                } else {
                    Log.e("performLogout", "Nieudane wylogowanie, kod odpowiedzi: ${response.code()}")
                    Log.e("performLogout", "Błąd podczas wylogowania: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Błąd podczas wylogowania", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("performLogout", "Błąd połączenia z serwerem", t)
                Toast.makeText(this@ProfileActivity, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show()
            }
        })
        logoutUserLocally(sharedPref)
    }

    private fun logoutUserLocally(sharedPref: SharedPreferences) {
        with(sharedPref.edit()) {
            remove("username")
            remove("password")
            remove("session_id")
            putBoolean("remember_me", false)
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
