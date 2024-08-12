package com.example.thesisapp

import ApiClient
import RegisterRequest
import RegisterResponse
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnChangeLanguage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl") // Domyślnie polski

        // Ustawienie wybranego języka
        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        etUsername = findViewById(R.id.etRegisterUsername)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)

        btnRegister.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            } else {
                val username = etUsername.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val registerRequest = RegisterRequest(username, email, password)

                    // Utwórz instancję ApiClient i wywołaj metodę getApiService()
                    val apiClient = ApiClient(this)
                    val apiService = apiClient.getApiService()

                    apiService.register(registerRequest)
                        .enqueue(object : Callback<RegisterResponse> {
                            override fun onResponse(
                                call: Call<RegisterResponse>,
                                response: Response<RegisterResponse>
                            ) {
                                if (response.isSuccessful) {
                                    response.body()?.let {
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            "Registration successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(
                                            Intent(
                                                this@RegisterActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finish()
                                    } ?: Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration failed: Empty response",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration failed: ${response.errorBody()?.string()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Error: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }

        btnChangeLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("previous_activity", "RegisterActivity")
            startActivity(intent)
        }


        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}
