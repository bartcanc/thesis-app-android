package com.example.thesisapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private var previousActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        previousActivity = intent.getStringExtra("previous_activity")

        val tvChooseLanguage = findViewById<TextView>(R.id.tvSelectLanguage)
        tvChooseLanguage.text = getString(R.string.select_language)

        findViewById<LinearLayout>(R.id.btnPolish).setOnClickListener {
            setLocale("pl")
        }
        findViewById<LinearLayout>(R.id.btnEnglish).setOnClickListener {
            setLocale("en")
        }
        findViewById<LinearLayout>(R.id.btnGerman).setOnClickListener {
            setLocale("de")
        }
        findViewById<LinearLayout>(R.id.btnSpanish).setOnClickListener {
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

        val intent = when (previousActivity) {
            "LoginActivity" -> Intent(this, LoginActivity::class.java)
            "MainActivity" -> Intent(this, MainActivity::class.java)
            "NoConnectionActivity" -> Intent(this, NoConnectionActivity::class.java)
            "RegisterActivity" -> Intent(this, RegisterActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
