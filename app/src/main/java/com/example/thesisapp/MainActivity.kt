package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class MainActivity : BaseActivity() {
    private lateinit var tvMessage: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var btnPasswordReset: Button
    private lateinit var btnConnect: Button
    private lateinit var btnReadData: Button
    private lateinit var btnSyncTime: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkSessionValidity()

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        tvMessage = findViewById(R.id.tvMessage)
        btnLogout = findViewById(R.id.btnLogout)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnPasswordReset = findViewById(R.id.btnPasswordReset)
        btnConnect = findViewById(R.id.btnConnect)
        btnReadData = findViewById(R.id.btnReadData)
        btnSyncTime = findViewById(R.id.btnSyncTime)

        // Po kliknięciu otwieramy ustawienia Bluetooth
        btnConnect.setOnClickListener {
            openBluetoothSettings()
        }

        btnSyncTime.setOnClickListener{
            sendUnixTime()
        }
        // Rozpoczynamy odczytywanie danych po kliknięciu
        btnReadData.setOnClickListener {
            sendUnixTime()
            startReadingLoop() // Funkcja z BaseActivity
        }
        // Retrieve the login message if available
        val message = intent.getStringExtra("message")
        if (message != null) {
            tvMessage.text = message
        }

        btnLogout.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            } else {
                with(sharedPref.edit()) {
                    remove("username")
                    remove("password")
                    putBoolean("remember_me", false)
                    apply()
                }
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnChangeLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("previous_activity", "MainActivity")
            startActivity(intent)
        }

        btnPasswordReset.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
            finish()
        }

    }



}