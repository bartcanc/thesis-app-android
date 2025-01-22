package com.example.thesisapp

import ApiClient
import Workout
import WorkoutAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrainingListActivity : BaseActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var recyclerTrainings: RecyclerView
    private val workouts = mutableListOf<Workout>()

    private var currentPage = 1
    private var isLastPage = false

    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_list)

        btnBack = findViewById(R.id.btnBack)
        recyclerTrainings = findViewById(R.id.recyclerTrainings)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        setupRecyclerView()

        btnBack.setOnClickListener { finish() }

        btnPrevious.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                fetchWorkouts()
            }
        }

        btnNext.setOnClickListener {
            if (!isLastPage) {
                currentPage++
                fetchWorkouts()
            }
        }

        fetchWorkouts()
    }

    private fun setupRecyclerView() {
        recyclerTrainings.layoutManager = LinearLayoutManager(this)
        recyclerTrainings.adapter = WorkoutAdapter(workouts) { workout ->
            navigateToSingleTraining(workout)
        }
    }

    private fun fetchWorkouts() {
        val userId = getUserID()
        val sessionId = getSessionID()

        if (userId != null && sessionId != null) {
            val apiClient = ApiClient(this)
            val apiService = apiClient.getApiService8000()

            // Dodaj parametry stronicowania: `page` i `limit`
            apiService.getTrainings(userId, currentPage).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val responseData = responseBody.string()
                            Log.d("fetchWorkouts", "Response data: $responseData")
                            parseAndDisplayWorkouts(responseData)
                        }
                    } else {
                        Log.e("fetchWorkouts", "Failed response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@TrainingListActivity, "Failed to fetch workouts", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("fetchWorkouts", "Error fetching workouts: ${t.message}")
                    Toast.makeText(this@TrainingListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User ID or Session ID not found", Toast.LENGTH_SHORT).show()
        }
    }




    private fun parseAndDisplayWorkouts(responseData: String) {
        try {
            val jsonObject = JSONObject(responseData)
            val workoutsArray = jsonObject.getJSONArray("workouts")
            var totalWorkouts = 0

            workouts.clear()
            if(workoutsArray.length()==0){ // jezeli nie odebralismy nic, to znaczy ze
                isLastPage = true           // jestesmy na ostatniej stronie
                currentPage--
            }
            else {
                isLastPage=false
                for (i in 0 until workoutsArray.length()) {
                    val workoutJson = workoutsArray.getJSONObject(i)
                    val workoutId = workoutJson.optInt("workoutId", 0)
                    val workoutType = workoutJson.optString("workoutType", "Unknown")
                    val duration = workoutJson.optInt("duration", 0)
                    val distance = workoutJson.optDouble("distance", 0.0)
                    val caloriesBurned = workoutJson.optDouble("caloriesBurned", 0.0)
                    val date = workoutJson.optString("date", "Unknown")
                    val avgSteps = workoutJson.optInt("avgSteps", 0)
                    val avgHeartrate = workoutJson.optDouble("avgHeartrate", 0.0)

                    workouts.add(
                        Workout(
                            workoutId = workoutId,
                            workoutType = workoutType,
                            duration = duration,
                            distance = distance,
                            caloriesBurned = caloriesBurned,
                            date = date,
                            avgSteps = avgSteps,
                            avgHeartrate = avgHeartrate
                        )
                    )
                    totalWorkouts++
                }
                recyclerTrainings.adapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e("parseWorkouts", "Error parsing workouts: ${e.message}")
        }
    }



    private fun navigateToSingleTraining(workout: Workout) {
        navigateToWorkoutDetails(
            workoutId = workout.workoutId,
            workoutType = workout.workoutType,
            duration = workout.duration,
            distance = workout.distance,
            caloriesBurned = workout.caloriesBurned,
            date = workout.date,
            avgSteps = workout.avgSteps,
            avgHeartrate = workout.avgHeartrate
        )
    }

    private fun navigateToWorkoutDetails(
        workoutId: Int,
        workoutType: String,
        duration: Int,
        distance: Double,
        caloriesBurned: Double,
        date: String,
        avgSteps: Int,
        avgHeartrate: Double
    ) {
        val intent = Intent(this, SingleTrainingActivity::class.java).apply {
            putExtra("TRAINING_ID", workoutId)
            putExtra("WORKOUT_TYPE", workoutType)
            putExtra("DURATION", duration)
            putExtra("DISTANCE", distance)
            putExtra("CALORIES_BURNED", caloriesBurned)
            putExtra("DATE", date)
            putExtra("AVG_STEPS", avgSteps)
            putExtra("AVG_HEARTRATE", avgHeartrate)
        }
        startActivity(intent)
    }
}
