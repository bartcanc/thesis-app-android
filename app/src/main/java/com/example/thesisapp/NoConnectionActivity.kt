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
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("previous_activity", "NoConnectionActivity")
            startActivity(intent)
        }

    }

}
