package com.example.thesisapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thesisapp.NetworkUtils.isNetworkAvailable
import java.util.Locale

class NoConnectionActivity : AppCompatActivity() {
    private lateinit var btnChangeLanguage: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl") // Domyślnie polski

        // Ustawienie wybranego języka
        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        if (isNetworkAvailable(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            findViewById<Button>(R.id.btnRetry).setOnClickListener {
                if (isNetworkAvailable(this)) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Nadal brak połączenia", Toast.LENGTH_SHORT).show()
                }
            }

            findViewById<Button>(R.id.btnSettings).setOnClickListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) // Przekierowanie do ustawień Wi-Fi
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
