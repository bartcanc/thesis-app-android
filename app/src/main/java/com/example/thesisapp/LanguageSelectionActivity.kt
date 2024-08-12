package com.example.thesisapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private var previousActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        sharedPref = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        // Pobierz nazwę poprzedniej aktywności
        previousActivity = intent.getStringExtra("previous_activity")

        // Ustaw dynamicznie tekst "Wybierz język" zależnie od obecnego języka
        val tvChooseLanguage = findViewById<TextView>(R.id.tvChooseLanguage)
        tvChooseLanguage.text = getString(R.string.select_language)

        findViewById<ImageButton>(R.id.btnPolish).setOnClickListener {
            setLocale("pl")
        }

        findViewById<ImageButton>(R.id.btnEnglish).setOnClickListener {
            setLocale("en")
        }

        findViewById<ImageButton>(R.id.btnGerman).setOnClickListener {
            setLocale("de")
        }

        findViewById<ImageButton>(R.id.btnSpanish).setOnClickListener {
            setLocale("es")
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        with(sharedPref.edit()) {
            putString("selected_language", languageCode)
            apply()
        }

        // Powrót do poprzedniej aktywności
        val intent = when (previousActivity) {
            "LoginActivity" -> Intent(this, LoginActivity::class.java)
            "MainActivity" -> Intent(this, MainActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
