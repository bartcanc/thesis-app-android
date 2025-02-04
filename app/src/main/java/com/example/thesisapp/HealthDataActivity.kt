package com.example.thesisapp

import ApiClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HealthDataActivity : BaseActivity() {

    private lateinit var genderInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvHeader: TextView

    private lateinit var spActivityLevel: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_data)

        val rootLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
        // lub jakikolwiek inny "główny" layout z Twojego XML-a

        // Przykładowy odczyt z SharedPreferences
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea") // domyślnie "sea"

        // Jeżeli to jest "post modern", zmieniamy background:
        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            // Sea Breeze (domyślnie)
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        // Inicjalizacja widoków
        genderInput = findViewById(R.id.etGender)
        ageInput = findViewById(R.id.etAge)
        weightInput = findViewById(R.id.etWeight)
        heightInput = findViewById(R.id.etHeight)
        btnSubmit = findViewById(R.id.btnConfirmData)
        tvHeader = findViewById(R.id.tvHeader)

        spActivityLevel = findViewById(R.id.spActivityLevel)

        // Pobierz nazwę poprzedniej aktywności
        val previousActivity = intent.getStringExtra("previous_activity")

        // Ustaw nagłówek i logikę nawigacji na podstawie poprzedniej aktywności
        if (previousActivity == "LoginActivity") {
            tvHeader.text = getString(R.string.enter_your_data)

        } else if (previousActivity == "SettingsActivity") {
            tvHeader.text = getString(R.string.edit_your_data)
        }

        val activityLevels = resources.getStringArray(R.array.activity_levels)

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            activityLevels
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spActivityLevel.setPopupBackgroundResource(android.R.color.transparent)
        spActivityLevel.adapter = adapter

        btnSubmit.setOnClickListener {
            handleFormSubmission {
                startActivity(Intent(this@HealthDataActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun handleFormSubmission(onSuccess: () -> Unit) {
        val userId = sharedPref.getString("user_id", null)

        // Pobierz wartości z pól tekstowych
        val gender = genderInput.text.toString()
        val age = ageInput.text.toString().toIntOrNull()
        val weight = weightInput.text.toString().toIntOrNull()
        val height = heightInput.text.toString().toIntOrNull()

        val pal = when (spActivityLevel.selectedItem.toString()) {
            getString(R.string.activity_v_low) -> 1.3f // Very low (0-1 trainings/week)
            getString(R.string.activity_low) -> 1.4f     // Low (2-3 trainings/week)
            getString(R.string.activity_medium) -> 1.6f  // Medium (4-5 trainings/week)
            getString(R.string.activity_high) -> 1.75f   // High (6-7 trainings/week)
            else -> 2f                                   // Very high activity
        }

        if (userId != null) {
            if (gender.isNotEmpty() && age != null && weight != null && height != null) {
                // Tworzenie JSON-a dla ciała zapytania
                val requestBodyJson = JSONObject().apply{
                        put("userId", userId)
                        put("gender", gender)
                        put("age", age)
                        put("weight", weight)
                        put("height", height)
                        put("bmr", 0)
                        put("tdee", 0)
                        put("activity", pal)
                }

                val requestBody = requestBodyJson.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val apiClient = ApiClient(this)
                val apiService = apiClient.getApiService8000()

                // Wysyłanie zapytania PUT z `userId` jako parametrem query
                apiService.sendHealthData(userId, requestBody)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("HealthDataActivity", "Server response: ${response.body()?.string()}")
                                Toast.makeText(
                                    this@HealthDataActivity,
                                    "Health data sent successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onSuccess() // Wywołanie sukcesu
                            } else {
                                Log.e("HealthDataActivity", "Failed response: ${response.errorBody()?.string()}")
                                Toast.makeText(
                                    this@HealthDataActivity,
                                    "Failed to send health data: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Log.e("HealthDataActivity", "Error: ${t.message}")
                            Toast.makeText(
                                this@HealthDataActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }


}
