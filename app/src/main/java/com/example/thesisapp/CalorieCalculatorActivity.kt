package com.example.thesisapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class CalorieCalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ukryj pasek akcji, jeśli jeszcze widoczny
        supportActionBar?.hide()

// Layout fullscreen z zachowaniem paska systemowego w postaci nakładki
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

// Dodatkowo przezroczysty status bar:
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_calorie_calculator)

        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etAge = findViewById<EditText>(R.id.etAge)
        val spActivityLevel = findViewById<Spinner>(R.id.spActivityLevel)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)

        val llMale = findViewById<LinearLayout>(R.id.llGender).findViewById<FrameLayout>(R.id.flMale)
        val llFemale = findViewById<LinearLayout>(R.id.llGender).findViewById<FrameLayout>(R.id.flFemale)

        val imgMaleCheck = findViewById<ImageView>(R.id.imgMaleCheck)
        val imgFemaleCheck = findViewById<ImageView>(R.id.imgFemaleCheck)

        val activityLevels = resources.getStringArray(R.array.activity_levels)

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            activityLevels
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spActivityLevel.setPopupBackgroundResource(android.R.color.transparent)
        spActivityLevel.adapter = adapter

        llMale.setOnClickListener {
            imgMaleCheck.visibility = View.VISIBLE
            imgFemaleCheck.visibility = View.GONE
        }

        llFemale.setOnClickListener {
            imgMaleCheck.visibility = View.GONE
            imgFemaleCheck.visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCalculate.setOnClickListener{
            val weight = etWeight.text.toString().toFloatOrNull() // Waga w kg
            val height = etHeight.text.toString().toIntOrNull() // Wzrost w centymetrach
            val age = etAge.text.toString().toIntOrNull()
            var gender = ""
            if(imgFemaleCheck.visibility == View.VISIBLE && imgMaleCheck.visibility == View.GONE){
                gender ="F"
            }
            else if(imgMaleCheck.visibility == View.VISIBLE && imgFemaleCheck.visibility == View.GONE){
                gender ="M"
            }
            val pal = when (spActivityLevel.selectedItem.toString()) {
                getString(R.string.activity_v_low) -> 1.3f // Very low (0-1 trainings/week)
                getString(R.string.activity_low) -> 1.4f     // Low (2-3 trainings/week)
                getString(R.string.activity_medium) -> 1.6f  // Medium (4-5 trainings/week)
                getString(R.string.activity_high) -> 1.75f   // High (6-7 trainings/week)
                else -> 2f                                   // Very high activity
            }

            if (weight != null && height != null && age != null && gender != "") {
                val calculatedCalories = calculateCalories(weight, height, age, gender, pal)
                showCalorieAlert(calculatedCalories)
            } else {
                Toast.makeText(this, "Please enter valid weight and height!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateCalories(weight: Float, height: Int, age: Int, gender: String, pal: Float): Double {
        if(gender == "M"){
            return (66 + (13.7*weight) + (5*height) - (6.8*age)) * pal
        }
        else if(gender == "F"){
            return (655 + (9.6*weight) + (1.8*height) - (4.7*age)) * pal
        }
        return 0.0
    }

    private fun showCalorieAlert(calories: Double) {
        // Inflate the alert dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.calorie_result_dialog, null)

        // Build the alert dialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find and set the values for the TextViews in the dialog
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvResultValue = dialogView.findViewById<TextView>(R.id.tvResultValue)
        val tvAdditionalInfo = dialogView.findViewById<TextView>(R.id.tvAdditionalInfo)
        val btnCloseDialog = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        // Set text for dialog elements
        tvDialogTitle.text = "Your Calorie\nincome is:"
        tvResultValue.text = calories.toInt().toString()
        tvAdditionalInfo.text = "kcal"

        // Close the dialog when the button is clicked
        btnCloseDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        // Show the dialog
        alertDialog.show()
    }
}
