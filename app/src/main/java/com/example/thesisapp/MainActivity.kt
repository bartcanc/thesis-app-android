package com.example.thesisapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity: BaseActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBand = findViewById<LinearLayout>(R.id.btnBand)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnMyTrainings = findViewById<LinearLayout>(R.id.llMyTrainings)
        val btnBMICalories = findViewById<LinearLayout>(R.id.llBMICalculator)
        val btnForum = findViewById<LinearLayout>(R.id.llForum)
        val btnBadges = findViewById<LinearLayout>(R.id.llBadges)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        val username = sharedPref.getString("username", "User") // Domyślna wartość to "User"
        tvWelcome.text = "Welcome $username!"

        btnMyTrainings.setOnClickListener{
            val intent = Intent(this, TrainingListActivity::class.java)
            startActivity(intent)
        }

        btnBMICalories.setOnClickListener{
            val intent = Intent(this, BMICaloriesActivity::class.java)
            startActivity(intent)
        }

        // Przyciski nawigacji
        btnSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBand.setOnClickListener{
            val intent = Intent(this, BandActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnForum.setOnClickListener {
            Toast.makeText(this, "This feature will be added in the future.", Toast.LENGTH_SHORT).show()
        }

        btnBadges.setOnClickListener {
            Toast.makeText(this, "This feature will be added in the future.", Toast.LENGTH_SHORT).show()
        }
    }
}