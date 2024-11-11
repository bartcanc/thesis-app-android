package com.example.thesisapp

import ApiClient
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var bmrInput: EditText
    private lateinit var tdeeInput: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_data)

        genderInput = findViewById(R.id.genderInput)
        ageInput = findViewById(R.id.ageInput)
        weightInput = findViewById(R.id.weightInput)
        heightInput = findViewById(R.id.heightInput)
        bmrInput = findViewById(R.id.bmrInput)
        tdeeInput = findViewById(R.id.tdeeInput)
        btnSubmit = findViewById(R.id.submitButton)

        btnSubmit.setOnClickListener {
            val sharedPref = getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", null)

            // Pobierz wartości z pól tekstowych przy każdym kliknięciu przycisku
            val gender = genderInput.text.toString()
            val age = ageInput.text.toString().toIntOrNull()
            val weight = weightInput.text.toString().toFloatOrNull()
            val height = heightInput.text.toString().toFloatOrNull()
            val bmr = bmrInput.text.toString().toFloatOrNull()
            val tdee = tdeeInput.text.toString().toFloatOrNull()

            if (userId != null) {
                if (gender != "" && age != null && weight != null && height != null && bmr != null && tdee != null) {
                    val healthDataRequest = HealthDataRequest(
                        userId = userId,
                        gender = gender,
                        age = age,
                        weight = weight,
                        height = height,
                        bmr = bmr,
                        tdee = tdee
                    )

                    val apiClient = ApiClient(this)
                    val apiService = apiClient.getApiService8000()

                    apiService.sendHealthData(healthDataRequest)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    val responseBody = response.body()?.string()
                                    Log.d("HealthDataActivity", "Server response: $responseBody")

                                    Toast.makeText(
                                        this@HealthDataActivity,
                                        "Health data sent successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this@HealthDataActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("HealthDataActivity", "Failed response: $errorBody")
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
                    Toast.makeText(
                        this,
                        "Please fill all fields correctly",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
