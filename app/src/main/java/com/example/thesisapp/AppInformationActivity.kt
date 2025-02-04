package com.example.thesisapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout

class AppInformationActivity : BaseActivity() {
    private lateinit var btnBack: AppCompatImageButton

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
        setContentView(R.layout.activity_app_info)

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

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }

}
