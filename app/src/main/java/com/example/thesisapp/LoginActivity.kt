package com.example.thesisapp

import ApiClient
import LoginRequest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : BaseActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var switchRememberMe: Switch

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //checkSessionValidity()

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        etUsername = findViewById(R.id.etLoginUsername)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        switchRememberMe = findViewById(R.id.switchRememberMe)

        loadLoginData()

        btnLogin.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            } else {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    performLogin(username, password)
                }
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnChangeLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("previous_activity", "LoginActivity")
            startActivity(intent)
        }

    }

    private fun performLogin(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        apiService.login(loginRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rememberMe = sharedPref.getBoolean("remember_me", false)
                    if (rememberMe) {
                        saveLoginData(username, password)
                    } else {
                        clearLoginData()
                    }

                    response.body()?.let { body ->
                        val jsonResponse = JSONObject(body.string())
                        val userId = jsonResponse.optString("user_id", "")  // Pobieramy `user_id` z JSON
                        if (userId.isNotEmpty()) {
                            Log.d("USERID", "USTAWIONO USERID")
                            setUserID(userId)
                        }
                    }

                    // Pobranie `session-id` z nagłówków odpowiedzi
                    val sessionId = response.headers()["session_id"]
                    Log.d("performLogin", "session-id from response header: $sessionId")  // Logujemy `session-id` z odpowiedzi

                    if (sessionId != null) {
                        with(sharedPref.edit()) {
                            putString("session_id", sessionId)
                            apply()
                        }
                        Log.d("performLogin", "session-id saved in SharedPreferences: $sessionId")
                    } else {
                        Log.e("performLogin", "session-id not found in response headers!")
                    }

                  // Sprawdzenie, czy użytkownik istnieje w bazie danych dopiero po zapisaniu userId i session-id
            checkUserExistsInDatabase(
                getUserID()!!,
                onSuccess = {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                },
                onFailure = {
                    startActivity(Intent(this@LoginActivity, HealthDataActivity::class.java))
                    finish()
                }
            )
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed 😔: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message} 😢", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun saveLoginData(username: String, password: String) {
        with(sharedPref.edit()) {
            putString("username", username)
            putString("password", password)
            putBoolean("remember_me", true)
            apply()
        }
    }

    private fun loadLoginData() {
        val rememberMe = sharedPref.getBoolean("remember_me", false)
        if (rememberMe) {
            etUsername.setText(sharedPref.getString("username", ""))
            etPassword.setText(sharedPref.getString("password", ""))
            switchRememberMe.isChecked = true
        }
    }

    // Funkcja do sprawdzania istnienia użytkownika w bazie danych
    private fun checkUserExistsInDatabase(userId: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        // Pobierz session-id z SharedPreferences
        val sessionId = sharedPref.getString("session_id", null)

        if (sessionId.isNullOrEmpty()) {
            Log.e("LoginActivity", "Session ID is missing in SharedPreferences.")
            onFailure()
            return
        }

        // Wywołanie API z nagłówkiem session-id i parametrem userId
        apiService.getUserHealthById(userId, sessionId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                when (response.code()) {
                    200 -> {
                        // Kod 200: użytkownik istnieje
                        Log.d("checkUserExistsInDatabase", "User exists in database.")
                        onSuccess()
                    }
                    else -> {
                        // Inne kody: użytkownik nie istnieje
                        Log.d("checkUserExistsInDatabase", "User does not exist in database. Response code: ${response.code()}")
                        onFailure()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("LoginActivity", "Error checking user existence: ${t.message}")
                onFailure()
            }
        })
    }


}
