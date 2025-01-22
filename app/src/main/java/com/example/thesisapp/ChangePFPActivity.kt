package com.example.thesisapp

import android.os.Bundle
import android.widget.ImageButton

class ChangePFPActivity :BaseActivity() {
    private lateinit var btnBack: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pfp)

        btnBack =findViewById(R.id.btnBack)

        btnBack.setOnClickListener{
            finish()
        }
    }
}