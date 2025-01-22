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

        // Ustawienie języka aplikacji
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")
        setAppLocale(selectedLanguage ?: "pl")

        // Inicjalizacja widoków
        codeDisplay = findViewById(R.id.tvResetCode)
        codeInfo = findViewById(R.id.tvCustomMessage)
        nextButton = findViewById(R.id.btnOk)

        // Pobieranie kodu resetu
        val passCode = sharedPref.getString("passResetCode", "")
        Log.i("CodeActivity", "Pass reset code: $passCode")

        // Wyświetlenie kodu resetu lub komunikatu, jeśli brak kodu
        if (!passCode.isNullOrEmpty()) {
            codeDisplay.text = passCode
        } else {
            codeDisplay.text = "skibidi"
        }

        // Obsługa przycisku "Next"
        nextButton.setOnClickListener {
            clearPassResetCode()
            navigateToLogin()
        }
    }

    /**
     * Ustawienie lokalizacji aplikacji na wybrany język.
     */
    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Czyszczenie kodu resetu w pamięci współdzielonej.
     */
    private fun clearPassResetCode() {
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("passResetCode", "")
            apply()
        }
    }

    /**
     * Nawigacja do ekranu logowania.
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}