package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class BMICaloriesActivity: AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calorie_calc)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnBMI = findViewById<LinearLayout>(R.id.btnBMICalculator)
        val btnCal = findViewById<LinearLayout>(R.id.btnCaloriesCalculator)

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
