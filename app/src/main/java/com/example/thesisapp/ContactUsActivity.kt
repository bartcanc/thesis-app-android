package com.example.thesisapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageButton

class ContactUsActivity: BaseActivity() {
    private lateinit var btnBack: AppCompatImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}
