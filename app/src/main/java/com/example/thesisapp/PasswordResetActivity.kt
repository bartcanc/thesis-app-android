package com.example.thesisapp

import ApiClient
import PasswordResetRequest
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class PasswordResetActivity: BaseActivity() {
    private lateinit var username: String
    private lateinit var resetCode: EditText
    private lateinit var newPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var btnReturn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        val rootLayout = findViewById<FrameLayout>(R.id.frameLayout)
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

        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        username = sharedPref.getString("username", "null").toString()
        resetCode = findViewById(R.id.etResetPasswordCode)
        newPassword = findViewById(R.id.etNewPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnReturn = findViewById(R.id.btnBack)

        btnResetPassword.setOnClickListener {
            performReset(username, resetCode, newPassword)
        }

        btnReturn.setOnClickListener {
            finish()
        }
    }

    private fun performReset(username: String, passwordResetCode: EditText, password: EditText) {
        val passwordResetRequest = PasswordResetRequest(username, passwordResetCode.text.toString(), newPassword.text.toString())

        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        apiService.password_change(passwordResetRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val passCode = response.body()?.string()?.let { JSONObject(it).getString("password_reset_code") }
                    with(sharedPref.edit()){
                        putString("password", password.text.toString())
                        putString("passResetCode", passCode)
                    }
                    Toast.makeText(this@PasswordResetActivity, "Password reset successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PasswordResetActivity, SettingsActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@PasswordResetActivity, "Password reset failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PasswordResetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
