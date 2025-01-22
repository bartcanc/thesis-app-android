package com.example.thesisapp

import ApiClient
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class GraphActivity : BaseActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var btnBack: ImageButton
    private lateinit var tvHeader: TextView
    private lateinit var tvTrainingName: TextView
    private lateinit var tvTrainingDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        btnBack = findViewById(R.id.btnBack)
        tvHeader = findViewById(R.id.tvGraphTitle)
        tvTrainingName = findViewById(R.id.tvTrainingTitle)
        tvTrainingDate = findViewById(R.id.tvTrainingDate)

        lineChart = findViewById(R.id.lineChart)

        val trainingType = intent.getStringExtra("TRAINING_TYPE") ?: "Nieznany trening"
        val trainingDate = intent.getStringExtra("TRAINING_DATE") ?: "Nieznana data"
        val graphType = intent.getStringExtra("GRAPH_TYPE") ?: "Nieznany typ wykresu"

        // Aktualizacja nagłówków
        updateHeaders(trainingType, trainingDate, graphType)

        fetchGraphData(trainingDate, graphType)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateHeaders(trainingType: String, trainingDate: String, graphType: String) {
        tvHeader.text = when (graphType) {
            "speed" -> "Speed Graph"
            "heartrate" -> "Heartrate Graph"
            else -> "Unknown Graph"
        }

        tvTrainingName.text = trainingType

        tvTrainingDate.text = try {
            val inputFormat = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

            val parsedDate = inputFormat.parse(trainingDate)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            Log.e("GraphActivity", "Błąd formatowania daty: ${e.message}")
            trainingDate
        }
    }

    private fun renderGraph(entries: List<Entry?>, description: String) {
        val lineDataSet = LineDataSet(entries, description)
        lineDataSet.color = resources.getColor(R.color.red_500) // Kolor linii
        lineDataSet.valueTextColor = resources.getColor(R.color.black) // Kolor tekstu
        lineDataSet.setDrawCircles(false) // Wyłączenie punktów na linii
        lineDataSet.lineWidth = 2f // Grubość linii

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.description.text = description
        lineChart.invalidate() // Odśwież wykres
    }

    private fun fetchGraphData(trainingDate: String, graphType: String) {
        val userId = getUserID()
        val sessionId = getSessionID()

        if (userId == null || sessionId == null) {
            Log.e("GraphActivity", "Brak userId lub sessionId")
            return
        }

        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        val formattedDate = formatDateToISO8601(trainingDate)

        apiService.getGraphData(userId, formattedDate, graphType).enqueue(object : Callback<GraphResponse> {
            override fun onResponse(call: Call<GraphResponse>, response: Response<GraphResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { graphResponse ->
                        Log.d("GraphActivity", "Otrzymana odpowiedź: $graphResponse")

                        when (graphType) {
                            "speed" -> {
                                val entries = graphResponse.graph_data.map { point ->
                                    point.speed?.let { Entry(point.time.toFloat(), it.toFloat()) }
                                }
                                renderGraph(entries, "Speed Over Time")
                            }
                            "heartrate" -> {
                                val entries = graphResponse.graph_data.map { point ->
                                    point.bpm?.let { Entry(point.time.toFloat(), it.toFloat()) }
                                }
                                renderGraph(entries, "Heartrate Over Time")
                            }
                            else -> {
                                Log.e("GraphActivity", "Nieznany typ grafu: $graphType")
                            }
                        }
                    }
                } else {
                    Log.e("GraphActivity", "Błąd podczas pobierania danych: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GraphResponse>, t: Throwable) {
                Log.e("GraphActivity", "Błąd połączenia: ${t.message}")
            }
        })
    }

    private fun formatDateToISO8601(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            Log.e("formatDateToISO8601", "Błąd formatowania daty: ${e.message}")
            date
        }
    }
}

data class GraphPoint(
    val time: Double,
    val speed: Double?,
    val bpm: Double?
)

data class GraphResponse(
    val graph_data: List<GraphPoint>
)
