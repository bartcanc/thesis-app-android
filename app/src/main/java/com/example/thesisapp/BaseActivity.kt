package com.example.thesisapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.ParseException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
open class BaseActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
    }

    protected fun checkSessionValidity() {
        val expirationDateStr = sharedPref.getString("expiration_date", null)
        if (expirationDateStr != null) {
            try {
                val sessionDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
                val expirationDate = sessionDateFormat.parse(expirationDateStr)
                val currentDate = Date()

                if (expirationDate != null && currentDate.after(expirationDate)) {
                    Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    clearLoginData()
                    with(sharedPref.edit()) {
                        remove("session_id")
                        remove("expiration_date")
                        apply()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(this, "Error parsing session expiration date.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearLoginData() {
        with(sharedPref.edit()) {
            remove("username")
            remove("password")
            remove("remember_me")
            apply()
        }
    }
}
