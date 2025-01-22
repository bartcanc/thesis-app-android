package com.example.thesisapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BMICalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etAge = findViewById<EditText>(R.id.etAge)
        val btnCalculateBMI = findViewById<Button>(R.id.btnCalculate)

        btnBack.setOnClickListener {
            finish()
        }


        btnCalculateBMI.setOnClickListener {
            val weight = etWeight.text.toString().toFloatOrNull() // Waga w kg
            val height = etHeight.text.toString().toIntOrNull() // Wzrost w centymetrach
            val age = etAge.text.toString().toIntOrNull()

            if (weight != null && height != null && age != null) {
                val bmi = calculateBMI(weight, height)
                showBMIDialog(bmi, age)
            } else {
                Toast.makeText(this, "Please enter valid weight and height!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateBMI(weight: Float, height: Int): Float {
        val heightInMeters = height / 100.0
        val bmi = weight / (heightInMeters * heightInMeters)
        return bmi.toFloat()
    }


    private fun showBMIDialog(bmi: Float, age: Int) {
        val dialogView = layoutInflater.inflate(R.layout.bmi_result_dialog, null)

        val tvBMIDescription = dialogView.findViewById<TextView>(R.id.tvAdditionalInfo)
        val tvBMIValue = dialogView.findViewById<TextView>(R.id.tvResultValue)
        val btnCloseDialog = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        // Ustaw wyniki BMI
        tvBMIValue.text = String.format("%.2f", bmi)

        // Pobierz opis BMI
        val description = getBMIDescription(bmi, age)
        tvBMIDescription.text = description

        // Ustawienie koloru tekstu na podstawie zasobów string
        val textColor = when (description) {
            getString(R.string.bmi_status_underweight) -> android.graphics.Color.BLUE
            getString(R.string.bmi_status_normal) -> android.graphics.Color.GREEN
            getString(R.string.bmi_status_overweight) -> android.graphics.Color.YELLOW
            getString(R.string.bmi_status_obesity) -> android.graphics.Color.RED
            else -> android.graphics.Color.BLACK // Defaultowy kolor
        }
        tvBMIDescription.setTextColor(textColor)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun getBMIDescription(bmi: Float, age: Int): String {
        val normalRange = when (age) {
            in 19..24 -> 19f..24f
            in 25..34 -> 20f..25f
            in 35..44 -> 21f..26f
            in 45..54 -> 22f..27f
            in 55..64 -> 23f..28f
            else -> 24f..29f // 65 lat i więcej
        }

        return when {
            bmi < normalRange.start -> getString(R.string.bmi_status_underweight)
            bmi in normalRange -> getString(R.string.bmi_status_normal)
            bmi > normalRange.endInclusive && bmi <= 31f -> getString(R.string.bmi_status_overweight)
            bmi > 31 -> getString(R.string.bmi_status_obesity)
            else -> "Status: Unidentified"
        }
    }

}
