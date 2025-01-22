package com.example.thesisapp

import ApiClient
import RegisterRequest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
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
    private lateinit var btnReturn: AppCompatImageButton

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
        btnReturn = findViewById(R.id.btnReturn)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val passwordRepeat = etPasswordRepeat.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != passwordRepeat) {
                Toast.makeText(this, "Both passwords must be identical", Toast.LENGTH_SHORT).show()
            } else {
                performRegister(username, password)
            }
        }

        btnReturn.setOnClickListener {
            finish()
        }
    }

    private fun performRegister(username: String, password: String){
        val registerRequest = RegisterRequest(username, password)
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        apiService.register(registerRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val responseString = it.string()
                            val jsonResponse = JSONObject(responseString)

                            val userId = jsonResponse.optString("user_id", "null")
                            val passCode = jsonResponse.optString("password_reset_code", "null")

                            if (userId.isNotEmpty()) {
                                showPasscodeDialog(passCode)    //wyswietlenie okna z kodem reset
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registration failed: Missing user ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showPasscodeDialog(passCode: String) {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_code, null)

        // Find and set the views in the dialog layout
        val tvResetCode = dialogView.findViewById<TextView>(R.id.tvResetCode)
        val tvCustomMessage = dialogView.findViewById<TextView>(R.id.tvCustomMessage)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        // Set the reset code and message dynamically
        tvResetCode.text = passCode
        tvCustomMessage.text = getString(R.string.reset_code_message)

        // Create and configure the AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set the background to be transparent
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Handle the OK button click
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Show the dialog
        alertDialog.show()
    }

}
