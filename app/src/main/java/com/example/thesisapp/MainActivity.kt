package com.example.thesisapp

import ApiClient
import ApiService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class MainActivity : BaseActivity() {
    private lateinit var tvMessage: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var btnPasswordReset: Button
    private lateinit var btnConnect: Button
    private lateinit var btnReadData: Button
    private lateinit var btnSyncTime: Button
    private lateinit var btnSaveData: Button
    private lateinit var btnSendData: Button
    private lateinit var btnSendWifiData: Button
    private lateinit var btnDisconnect: Button
    private lateinit var btnShowInfo: Button
    private lateinit var btnSettings: ImageButton

    private lateinit var apiService: ApiService


    @SuppressLint("MissingInflatedId", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiClient = ApiClient(this)
        apiService = apiClient.getApiService8000()
        //checkSessionValidity()

        val sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "pl")

        val locale = Locale(selectedLanguage ?: "pl")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        //tvMessage = findViewById(R.id.tvMessage)
        //btnLogout = findViewById(R.id.btnLogout)
        //btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        //btnPasswordReset = findViewById(R.id.btnPasswordReset)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        btnReadData = findViewById(R.id.btnReadData)
        btnSyncTime = findViewById(R.id.btnSyncTime)
        //btnSaveData = findViewById(R.id.btnSaveData)
        //btnSendData = findViewById(R.id.btnSendData)
        btnSendWifiData = findViewById(R.id.btnSendWifiData)
        btnShowInfo = findViewById(R.id.btnBandInfo)
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

        btnSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
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
//        btnSaveData.setOnClickListener {
//            createJSONFile()
//        }
//
//        btnSendData.setOnClickListener {
//            sendSensorData()
//        }
        // Retrieve the login message if available
        val message = intent.getStringExtra("message")
        if (message != null) {
            tvMessage.text = message
        }

//        btnLogout.setOnClickListener {
//            performLogout(sharedPref)
//        }
//
//        btnChangeLanguage.setOnClickListener {
//            val intent = Intent(this, LanguageSelectionActivity::class.java)
//            intent.putExtra("previous_activity", "MainActivity")
//            startActivity(intent)
//        }
//
//        btnPasswordReset.setOnClickListener {
//            startActivity(Intent(this, PasswordResetActivity::class.java))
//            finish()
//        }

        btnSendWifiData.setOnClickListener {
            sendWifiCredentials()
        }

        btnShowInfo.setOnClickListener {

        }
    }

    private fun performLogout(sharedPref: SharedPreferences) {
        apiService.logout().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // Loguj kod odpowiedzi i wiadomość
                Log.d("performLogout", "Response code: ${response.code()}")
                Log.d("performLogout", "Response message: ${response.message()}")

                if (response.isSuccessful && response.code() == 204) {
                    Log.d("performLogout", "Wylogowanie powiodło się.")
                    logoutUserLocally(sharedPref)
                } else {
                    // Loguj przypadki, gdy wylogowanie się nie powiodło mimo odpowiedzi z serwera
                    Log.e("performLogout", "Nieudane wylogowanie, kod odpowiedzi: ${response.code()}")
                    Log.e("performLogout", "Błąd podczas wylogowania: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "Błąd podczas wylogowania", Toast.LENGTH_SHORT).show()
                    logoutUserLocally(sharedPref)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Loguj błędy połączenia
                Log.e("performLogout", "Błąd połączenia z serwerem", t)
                Toast.makeText(this@MainActivity, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun logoutUserLocally(sharedPref: SharedPreferences) {
        with(sharedPref.edit()) {
            remove("username")
            remove("password")
            remove("session_id")
            putBoolean("remember_me", false)
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}