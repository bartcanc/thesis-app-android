package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import java.util.*

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private var previousActivity: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ukryj pasek akcji, jeśli jeszcze widoczny
        supportActionBar?.hide()

// Layout fullscreen z zachowaniem paska systemowego w postaci nakładki
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

// Dodatkowo przezroczysty status bar:
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_language_selection)

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
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("selected_language", languageCode)
            apply()
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        setResult(RESULT_OK)
        finish()
    }

}
