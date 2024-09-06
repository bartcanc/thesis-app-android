package com.example.thesisapp

import ApiClient
import RegisterRequest
import RegisterResponse
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPasswordRepeat: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnChangeLanguage: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        etUsername = findViewById(R.id.etRegisterUsername)
        etPassword = findViewById(R.id.etRegisterPassword)
        etPasswordRepeat = findViewById(R.id.etRegisterPasswordRepeat)
        btnRegister = findViewById(R.id.btnRegister)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)

        btnRegister.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            } else {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                val passwordRepeat = etPasswordRepeat.text.toString()

                if (username.isEmpty() ||  password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else if(password != passwordRepeat) {
                    Toast.makeText(this, "Both passwords must be identical",Toast.LENGTH_SHORT ).show()
                }
                else
                {
                    val registerRequest = RegisterRequest(username, password)
                    val apiClient = ApiClient(this)
                    val apiService = apiClient.getApiService()

                    apiService.register(registerRequest)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {

                                    response.body()?.let {
                                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                                        val passCode = response.body()?.string()?.let { JSONObject(it).getString("password_reset_code") }
//                                        Log.i("essa", passCode.toString())
                                        with(sharedPref.edit()){
                                            putString("passResetCode", passCode)
                                        }
                                        startActivity(
                                            Intent(this@RegisterActivity, CodeActivity::class.java)
                                        )
                                        finish()
                                    } ?: Toast.makeText(this@RegisterActivity, "Registration failed: Empty response", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@RegisterActivity, "Registration failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
