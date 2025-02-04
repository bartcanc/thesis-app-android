package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class BMICaloriesActivity: AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_bmi_calorie_calc)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnBMI = findViewById<LinearLayout>(R.id.btnBMICalculator)
        val btnCal = findViewById<LinearLayout>(R.id.btnCaloriesCalculator)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea") // domyślnie Sea Breeze

        if (selectedTheme == "post") {
            btnBMI.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
            btnCal.setBackgroundResource(R.drawable.rounded_button_background_post_modern)
        } else {
            btnBMI.setBackgroundResource(R.drawable.rounded_button_background_main)
            btnCal.setBackgroundResource(R.drawable.rounded_button_background_main)
        }

        btnBMI.setOnClickListener{
            val intent = Intent(this, BMICalculatorActivity::class.java)
            startActivity(intent)
        }

        btnCal.setOnClickListener{
            val intent = Intent(this, CalorieCalculatorActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener{
            finish()
        }
    }
}
