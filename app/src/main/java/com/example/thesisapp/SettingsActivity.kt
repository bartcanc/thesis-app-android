package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : BaseActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)

        val selectedTheme = sharedPref.getString("theme", "sea")

        if (selectedTheme == "post") {
            bottomNav.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            bottomNav.setBackgroundResource(R.drawable.gradient_background)
        }

        val changeUserDataOption = findViewById<TextView>(R.id.btnChangeUserData)
        val changeProfilePictureOption = findViewById<TextView>(R.id.btnChangeProfilePicture)
        val changePasswordOption = findViewById<TextView>(R.id.btnChangePassword)
        val changeLanguageOption = findViewById<TextView>(R.id.btnChangeLanguage)
        val changeThemeOption = findViewById<TextView>(R.id.btnChangeTheme)
        val appInfoOption = findViewById<TextView>(R.id.btnAppInfo)
        val contactUsOption = findViewById<TextView>(R.id.btnContactUs)

        val btnHome = findViewById<LinearLayout>(R.id.btnHome)
        val btnBand = findViewById<LinearLayout>(R.id.btnBand)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)

        // Obsługa opcji "Change user data"
        changeUserDataOption.setOnClickListener {
            val intent = Intent(this, EditHealthDataActivity::class.java)
            startActivity(intent)
        }

        // Obsługa opcji "Change profile picture"
        changeProfilePictureOption.setOnClickListener {
            val intent = Intent(this, ChangePFPActivity::class.java)
            startActivity(intent)
        }

        // Obsługa opcji "Change password"
        changePasswordOption.setOnClickListener {
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }

        // Obsługa opcji "Change language"
        changeLanguageOption.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivityForResult(intent, 1234)
        }

        // Obsługa opcji "Change theme"
        changeThemeOption.setOnClickListener {
            val intent = Intent(this, ChangeThemeActivity::class.java)
            startActivityForResult(intent, 1234)
        }

        // Obsługa opcji "App information"
        appInfoOption.setOnClickListener {
            val intent = Intent(this, AppInformationActivity::class.java)
            startActivity(intent)
        }

        // Obsługa opcji "Contact us"
        contactUsOption.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)
        }

        // Przyciski nawigacji
        btnHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBand.setOnClickListener{
            val intent = Intent(this, BandActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            recreate()
        }
    }

}