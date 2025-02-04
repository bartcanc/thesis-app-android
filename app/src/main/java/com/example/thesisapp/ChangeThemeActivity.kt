package com.example.thesisapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton

class ChangeThemeActivity : BaseActivity() {

    private lateinit var btnBack: AppCompatImageButton
    private lateinit var rootLayout: FrameLayout
    private lateinit var themeSeaBreeze: LinearLayout
    private lateinit var themePostModern: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_theme)

        // Inicjalizacja widoków
        rootLayout = findViewById(R.id.rootLayout)
        btnBack = findViewById(R.id.btnBack)
        themeSeaBreeze = findViewById(R.id.themeSeaBreeze)
        themePostModern = findViewById(R.id.themePostModern)

        // Odczyt z SharedPreferences (domyślnie "sea" jeśli nic nie zapisano)
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea")

        // Ustawiamy tło w zależności od wybranego motywu
        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            // Sea Breeze (domyślnie)
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        // Obsługa kliknięcia przycisku powrotu
        btnBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        // Kliknięcie w sekcję Sea Breeze
        themeSeaBreeze.setOnClickListener {
            sharedPref.edit()
                .putString("theme", "sea")
                .apply()

            // Odświeżenie aktywności, żeby od razu zobaczyć efekt
            recreate()
        }

        // Kliknięcie w sekcję Post Modern
        themePostModern.setOnClickListener {
            sharedPref.edit()
                .putString("theme", "post")
                .apply()

            // Odświeżenie aktywności
            recreate()
        }
    }
    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}
