package com.example.thesisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var tvMessage: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            tvMessage = findViewById(R.id.tvMessage)
            btnLogout = findViewById(R.id.btnLogout)

            // Retrieve the login message if available
            val message = intent.getStringExtra("message")
            if (message != null) {
                tvMessage.text = message
            }

            btnLogout.setOnClickListener {
                if (!NetworkUtils.isNetworkAvailable(this)) {
                    startActivity(Intent(this, NoConnectionActivity::class.java))
                    finish()
                } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
