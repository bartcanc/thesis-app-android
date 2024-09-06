package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import java.util.Locale

class CodeActivity: BaseActivity() {
    private lateinit var codeDisplay: TextView
    private lateinit var codeInfo: TextView
    private lateinit var nextButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)

        checkSessionValidity()

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        codeDisplay = findViewById(R.id.etCodeDisplay)
        nextButton = findViewById(R.id.nextButton)

        val passCode = sharedPref.getString("passResetCode", "")
        Log.i("essa", passCode.toString())

        codeDisplay.setText(passCode)

        nextButton.setOnClickListener {
            with(sharedPref.edit()){
                putString("passResetCode","")
                apply()
            }
            startActivity(Intent(this@CodeActivity, LoginActivity::class.java))
            finish()
        }
    }
}