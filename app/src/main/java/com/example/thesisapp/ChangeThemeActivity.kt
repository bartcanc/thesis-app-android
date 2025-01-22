package com.example.thesisapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageButton

class ChangeThemeActivity: BaseActivity() {
    private lateinit var btnBack: AppCompatImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_theme)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}
