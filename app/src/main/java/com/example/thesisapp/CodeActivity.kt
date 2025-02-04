package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import java.util.Locale

class CodeActivity: BaseActivity() {
    private lateinit var codeDisplay: TextView
    private lateinit var codeInfo: TextView
    private lateinit var nextButton: Button

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")
        setAppLocale(selectedLanguage ?: "pl")

        codeDisplay = findViewById(R.id.tvResetCode)
        codeInfo = findViewById(R.id.tvCustomMessage)
        nextButton = findViewById(R.id.btnOk)

        val passCode = sharedPref.getString("passResetCode", "")
        Log.i("CodeActivity", "Pass reset code: $passCode")

        if (!passCode.isNullOrEmpty()) {
            codeDisplay.text = passCode
        } else {
            codeDisplay.text = "skibidi"
        }

        nextButton.setOnClickListener {
            clearPassResetCode()
            navigateToLogin()
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun clearPassResetCode() {
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("passResetCode", "")
            apply()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}