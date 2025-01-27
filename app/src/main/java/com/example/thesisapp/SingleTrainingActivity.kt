package com.example.thesisapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.websocket.Frame
import java.text.SimpleDateFormat
import java.util.Locale

class SingleTrainingActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTrainingName: TextView
    private lateinit var tvTrainingDate: TextView
    private lateinit var btnSpeedGraph: LinearLayout
    private lateinit var btnHeartrateGraph: LinearLayout
    private lateinit var tvTimeValue: TextView
    private lateinit var tvCaloriesValue: TextView
    private lateinit var tvDistanceValue: TextView
    private lateinit var tvStepsValue: TextView
    private lateinit var tvBPMValue: TextView

    private lateinit var trainingID: String

    private var trainingName = ""
    private var trainingDate = ""
    private var timeValue = ""
    private var caloriesValue = ""
    private var distanceValue = ""
    private var stepsValue = ""
    private var bpmValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_training)

        // Inicjalizacja widoków
        btnBack = findViewById(R.id.btnBack)
        tvTrainingName = findViewById(R.id.tvTrainingName)
        tvTrainingDate = findViewById(R.id.tvTrainingDate)
        tvTimeValue = findViewById(R.id.tvTimeValue)
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue)
        tvDistanceValue = findViewById(R.id.tvDistanceValue)
        tvStepsValue = findViewById(R.id.tvStepsValue)
        tvBPMValue = findViewById(R.id.tvBPMValue)

        btnSpeedGraph = findViewById(R.id.btnSpeedGraph)
        btnHeartrateGraph = findViewById(R.id.btnHeartrateGraph)

        fetchIntentData()
        updateUI()

        btnBack.setOnClickListener {
            finish()
        }

        btnSpeedGraph.setOnClickListener {
            Toast.makeText(this, "Speed Graph clicked!", Toast.LENGTH_SHORT).show()
            navigateToGraphActivity("speed")
        }

        btnHeartrateGraph.setOnClickListener {
            Toast.makeText(this, "Heartrate Graph clicked!", Toast.LENGTH_SHORT).show()
            navigateToGraphActivity("heartrate")
        }
    }

    private fun fetchIntentData() {
        intent?.let {
            trainingName = it.getStringExtra("WORKOUT_TYPE") ?: "Unknown Training"
            trainingDate = it.getStringExtra("DATE") ?: "Unknown Date"
            timeValue = formatDuration(it.getIntExtra("DURATION", 0))
            caloriesValue = "${it.getDoubleExtra("CALORIES_BURNED", 0.0).toInt()} kcal"
            distanceValue = "${(it.getDoubleExtra("DISTANCE", 0.0)/1000)} m"
            stepsValue = it.getIntExtra("AVG_STEPS", 0).toString()
            bpmValue = "${it.getDoubleExtra("AVG_HEARTRATE", 0.0).toInt()} BPM"

            Log.d(
                "SingleTrainingActivity",
                "Data fetched: trainingName=$trainingName, trainingDate=$trainingDate, timeValue=$timeValue, " +
                        "caloriesValue=$caloriesValue, distanceValue=$distanceValue, stepsValue=$stepsValue, bpmValue=$bpmValue"
            )
        }
    }

    private fun updateUI() {
        tvTrainingName.text = trainingName
        tvTrainingDate.text = formatDate(trainingDate)
        tvTimeValue.text = timeValue
        tvCaloriesValue.text = caloriesValue
        tvDistanceValue.text = distanceValue
        tvStepsValue.text = stepsValue
        tvBPMValue.text = bpmValue

        Log.d(
            "updateUI",
            "UI updated: trainingName=$trainingName, trainingDate=$trainingDate, timeValue=$timeValue, " +
                    "caloriesValue=$caloriesValue, distanceValue=$distanceValue, stepsValue=$stepsValue, bpmValue=$bpmValue"
        )
    }

    private fun formatDuration(duration: Int): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.ENGLISH)

            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun navigateToGraphActivity(graphType: String) {
        val intent = Intent(this, GraphActivity::class.java).apply {
            putExtra("TRAINING_TYPE", trainingName)
            putExtra("TRAINING_DATE", trainingDate)
            putExtra("GRAPH_TYPE", graphType)
        }
        startActivity(intent)
    }
}
