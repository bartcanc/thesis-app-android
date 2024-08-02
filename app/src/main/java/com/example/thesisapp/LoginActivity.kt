package com.example.thesisapp

import LoginRequest
import LoginResponse
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var cbRememberMe: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("ThesisApp", Context.MODE_PRIVATE)

        etUsername = findViewById(R.id.etLoginUsername)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        cbRememberMe = findViewById(R.id.cbRemember)

        btnLogin.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            } else {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val loginRequest = LoginRequest(username, password)
                    ApiClient.apiService.login(loginRequest)
                        .enqueue(object : Callback<LoginResponse> {
                            override fun onResponse(
                                call: Call<LoginResponse>,
                                response: Response<LoginResponse>
                            ) {
                                if (response.isSuccessful) {  // Kod HTTP 200
                                    if (cbRememberMe.isChecked){
                                        with(sharedPref.edit()) {
                                            putString("USERNAME", username)
                                            putString("PASSWORD", password)
                                            putBoolean("REMEMBER_ME", true)
                                            apply()
                                        }
                                    } else {
                                        with(sharedPref.edit()) {
                                            remove("USERNAME")
                                            remove("PASSWORD")
                                            putBoolean("REMEMBER_ME", false)
                                            apply()
                                        }
                                    }
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.putExtra("message", "Successfully Logged In!")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val errorMessage =
                                        response.errorBody()?.string() ?: "Unknown error"
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login failed: $errorMessage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Network error: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        val sharedPref = getSharedPreferences("ThesisApp", Context.MODE_PRIVATE)
        val rememberMe = sharedPref.getBoolean("REMEMBER_ME", false)

        if (rememberMe) {
            val username = sharedPref.getString("USERNAME", "")
            val password = sharedPref.getString("PASSWORD", "")

            etUsername.setText(username)
            etPassword.setText(password)
            cbRememberMe.isChecked = true
            btnLogin.performClick()
        }
    }
}
