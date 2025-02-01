package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton

class ContactUsActivity : BaseActivity() {
    private lateinit var btnBack: AppCompatImageButton
    private lateinit var emailTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        btnBack = findViewById(R.id.btnBack)
        emailTextView = findViewById(R.id.tvEmail)

        btnBack.setOnClickListener {
            finish()
        }

        emailTextView.setOnClickListener {
            Log.d("ContactUsActivity", "Email TextView clicked")
            openEmailChooser()
        }
    }

    private fun openEmailChooser() {
        Log.d("ContactUsActivity", "openEmailChooser function invoked")
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("d3kapp.support@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Wsparcie aplikacji D3KApp")
        }
        val emailApps = packageManager.queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (emailApps.isNotEmpty()) {
            startActivity(Intent.createChooser(emailIntent, "Wybierz aplikację e-mail"))
        } else {
            Log.e("ContactUsActivity", "No email app available")
        }
    }
}
