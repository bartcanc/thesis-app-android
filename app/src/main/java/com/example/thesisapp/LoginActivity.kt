package com.example.thesisapp

import ApiClient
import LoginRequest
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var loginStatusImage: ImageView
    private lateinit var btnChangeLanguage: Button
    private lateinit var cbRememberMe: CheckBox

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etLoginUsername)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        loginStatusImage = findViewById(R.id.loginStatusImage)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        cbRememberMe = findViewById(R.id.cbRemember)

        loadLoginData()

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                if (cbRememberMe.isChecked) {
                    saveLoginData(username, password)
                } else {
                    clearLoginData()
                }
                performLogin(username, password)
            }
        }

        tvRegister.setOnClickListener {
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
        val apiService = apiClient.getApiService()

        apiService.login(loginRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val sessionId = response.headers().get("session_id")
                    if (sessionId != null) {
                        with(sharedPref.edit()) {
                            putString("session_id", sessionId)
                            apply()
                        }
                    }
//                    loginStatusImage.setImageResource(R.drawable.smile)
//                    loginStatusImage.visibility = ImageView.VISIBLE
                    Toast.makeText(this@LoginActivity, "Login successful! 🎉", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
//                    loginStatusImage.setImageResource(R.drawable.sad)
//                    loginStatusImage.visibility = ImageView.VISIBLE
                    Toast.makeText(this@LoginActivity, "Login failed 😔: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                loginStatusImage.setImageResource(R.drawable.sad)
//                loginStatusImage.visibility = ImageView.VISIBLE
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
            cbRememberMe.isChecked = true
        }
    }

    private fun clearLoginData() {
        with(sharedPref.edit()) {
            remove("username")
            remove("password")
            remove("remember_me")
            apply()
        }
    }
}
