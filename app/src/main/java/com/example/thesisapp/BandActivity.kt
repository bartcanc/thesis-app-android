package com.example.thesisapp

import ApiClient
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class BandActivity : BaseActivity() {
    private lateinit var tvMessage: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnPasswordReset: Button
    private lateinit var btnConnect: Button
    private lateinit var btnReadData: Button
    private lateinit var btnSyncTime: Button
    private lateinit var btnSaveData: Button
    private lateinit var btnSendData: Button
    private lateinit var btnSendWifiData: Button
    private lateinit var btnDisconnect: Button
    private lateinit var btnShowInfo: Button

    private lateinit var btnHome: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var btnSettings: LinearLayout


    @SuppressLint("MissingInflatedId", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_band_2)

        val apiClient = ApiClient(this)
        apiService = apiClient.getApiService8000()

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        //tvMessage = findViewById(R.id.tvMessage)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        btnReadData = findViewById(R.id.btnReadData)
        btnSyncTime = findViewById(R.id.btnSyncTime)
        btnSendData = findViewById(R.id.btnSendData)
        btnSendWifiData = findViewById(R.id.btnSendWifiData)
        btnShowInfo = findViewById(R.id.btnBandInfo)

        btnHome = findViewById(R.id.btnHome)
        btnProfile = findViewById(R.id.btnProfile)
        btnSettings = findViewById(R.id.btnSettings)

        // Po kliknięciu otwieramy ustawienia Bluetooth
        btnConnect.setOnClickListener {
            openBluetoothSettings()
        }

        btnDisconnect.setOnClickListener{
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        }

        btnSyncTime.setOnClickListener{
            sendUnixTime()
        }

        // Rozpoczynamy odczytywanie danych po kliknięciu
        btnReadData.setOnClickListener {
            if (bluetoothGatt != null) {
                Log.d("BLE", "Attempting to subscribe to characteristic.")
                subscribeToCharacteristic()
            } else {
                Toast.makeText(this, "Device not connected. Please connect first.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSendData.setOnClickListener {
            showTrainingTypeDialog()
        }

        // Retrieve the login message if available
        val message = intent.getStringExtra("message")
        if (message != null) {
            tvMessage.text = message
        }

        btnSendWifiData.setOnClickListener {
            sendWifiCredentials()
        }

        btnShowInfo.setOnClickListener {
            showBandInfoDialog()
        }

        // Przyciski nawigacji
        btnHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}