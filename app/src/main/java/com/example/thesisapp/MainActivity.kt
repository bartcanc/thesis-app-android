package com.example.thesisapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tvMessage: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnChangeLanguage: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl") // Domyślnie polski

        // Ustawienie wybranego języka
        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

            tvMessage = findViewById(R.id.tvMessage)
            btnLogout = findViewById(R.id.btnLogout)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)

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
                val sharedPref = getSharedPreferences("ThesisApp", Context.MODE_PRIVATE)

                with(sharedPref.edit()) {
                    remove("USERNAME")
                    remove("PASSWORD")
                    putBoolean("REMEMBER_ME", false)
                    apply()
                }
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnChangeLanguage.setOnClickListener {
            changeLanguage()  // Wywołanie zmiany języka
        }
    }

    private fun changeLanguage() {
        val currentLanguage = Locale.getDefault().language
        val newLocale = if (currentLanguage == "pl") Locale("en") else Locale("pl")

        // Ustawienie nowego języka
        val config = Configuration(resources.configuration)
        config.setLocale(newLocale)
        Locale.setDefault(newLocale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Zapisanie wybranego języka w SharedPreferences (opcjonalnie)
        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("selected_language", newLocale.language)
            apply()
        }

        // Restart Activity to apply changes
        val intent = intent
        finish()
        startActivity(intent)
    }

}
