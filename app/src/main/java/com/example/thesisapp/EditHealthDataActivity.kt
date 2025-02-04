package com.example.thesisapp

import ApiClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditHealthDataActivity : BaseActivity() {

    private lateinit var genderInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvHeader: TextView
    private lateinit var spActivityLevel: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_health_data)

        val rootLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea")

        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        genderInput = findViewById(R.id.etGender)
        ageInput = findViewById(R.id.etAge)
        weightInput = findViewById(R.id.etWeight)
        heightInput = findViewById(R.id.etHeight)
        btnSubmit = findViewById(R.id.btnConfirmData)
        tvHeader = findViewById(R.id.tvHeader)

        spActivityLevel = findViewById(R.id.spActivityLevel)
        spActivityLevel.context.setTheme(R.style.CustomSpinnerDialogTheme)

        val previousActivity = intent.getStringExtra("previous_activity")
        tvHeader.text = when (previousActivity) {
            "LoginActivity" -> getString(R.string.enter_your_data)
            "SettingsActivity" -> getString(R.string.edit_your_data)
            else -> ""
        }

        val activityLevels = listOf(getString(R.string.select_activity)) + resources.getStringArray(R.array.activity_levels)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, activityLevels)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spActivityLevel.setPopupBackgroundResource(android.R.color.transparent)
        spActivityLevel.adapter = adapter

        btnSubmit.setOnClickListener {
            handleFormSubmission {
                startActivity(Intent(this@EditHealthDataActivity, SettingsActivity::class.java))
                finish()
            }
        }
    }

    private fun handleFormSubmission(onSuccess: () -> Unit) {
        val userId = sharedPref.getString("user_id", null)
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = genderInput.text.toString().trim()
        val age = ageInput.text.toString().trim().toIntOrNull()
        val weight = weightInput.text.toString().trim().toIntOrNull()
        val height = heightInput.text.toString().trim().toIntOrNull()
        val pal = getActivityLevel()

        val userJson = JSONObject().apply {
            put("userId", userId)
            if (gender.isNotEmpty()) put("gender", gender)
            age?.let { put("age", it) }
            weight?.let { put("weight", it) }
            height?.let { put("height", it) }
            if (pal != null) put("activity", pal)
        }

        val requestBodyJson = JSONObject().apply {
            put("user", userJson)
        }

        sendDataToServer(userId, requestBodyJson, onSuccess)
    }

    private fun getActivityLevel(): Float? {
        return when (spActivityLevel.selectedItem.toString()) {
            getString(R.string.activity_v_low) -> 1.3f
            getString(R.string.activity_low) -> 1.4f
            getString(R.string.activity_medium) -> 1.6f
            getString(R.string.activity_high) -> 1.75f
            getString(R.string.select_activity) -> null
            else -> 2f
        }
    }

    private fun sendDataToServer(userId: String, requestBodyJson: JSONObject, onSuccess: () -> Unit) {
        val requestBody = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val apiClient = ApiClient(this)
        apiClient.getApiService8000().editHealthData(userId, requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("HealthDataActivity", "Server response: ${response.body()?.string()}")
                    Toast.makeText(this@EditHealthDataActivity, "Health data sent successfully", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Log.e("HealthDataActivity", "Failed response: ${response.errorBody()?.string()}")
                    Toast.makeText(this@EditHealthDataActivity, "Failed to send health data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HealthDataActivity", "Error: ${t.message}")
                Toast.makeText(this@EditHealthDataActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
