package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private var previousActivity: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        previousActivity = intent.getStringExtra("previous_activity")

        val tvChooseLanguage = findViewById<TextView>(R.id.tvHeader)
        tvChooseLanguage.text = getString(R.string.select_language)

        findViewById<MaterialButton>(R.id.btnPolish).setOnClickListener {
            setLocale("pl")
        }
        findViewById<MaterialButton>(R.id.btnEnglish).setOnClickListener {
            setLocale("en")
        }
        findViewById<MaterialButton>(R.id.btnGerman).setOnClickListener {
            setLocale("de")
        }
        findViewById<MaterialButton>(R.id.btnSpanish).setOnClickListener {
            setLocale("es")
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
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
        finish()
    }
}
