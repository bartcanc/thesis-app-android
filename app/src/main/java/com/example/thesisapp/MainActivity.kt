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
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

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
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("previous_activity", "MainActivity")
            startActivity(intent)
        }

    }

}
