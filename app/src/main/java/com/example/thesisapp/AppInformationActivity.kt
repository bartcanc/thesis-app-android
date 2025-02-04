package com.example.thesisapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton

class AppInformationActivity : BaseActivity() {
    private lateinit var btnBack: AppCompatImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_app_info)

        val rootLayout = findViewById<FrameLayout>(R.id.frameLayout)

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea") // domyślnie "sea"

        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }

}
