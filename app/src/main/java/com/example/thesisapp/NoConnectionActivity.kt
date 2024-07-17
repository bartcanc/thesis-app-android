package com.example.thesisapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thesisapp.NetworkUtils.isNetworkAvailable

class NoConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)
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
    }
}
