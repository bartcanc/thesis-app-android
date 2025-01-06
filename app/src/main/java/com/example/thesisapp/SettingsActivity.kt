package com.example.thesisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Obsługa przycisku powrotu
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Zakończenie aktywności i powrót do poprzedniego ekranu
        }

//        // Obsługa opcji "Change user data"
//        val changeUserDataOption = findViewById<LinearLayout>(R.id.tvChangeUserData)
//        changeUserDataOption.setOnClickListener {
//            // Przejście do aktywności zmiany danych użytkownika
//            val intent = Intent(this, ChangeUserDataActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Obsługa opcji "Change profile picture"
//        val changeProfilePictureOption = findViewById<LinearLayout>(R.id.tvChangeProfilePicture)
//        changeProfilePictureOption.setOnClickListener {
//            // Przejście do aktywności zmiany zdjęcia profilowego
//            val intent = Intent(this, ChangeProfilePictureActivity::class.java)
//            startActivity(intent)
//        }
//
        // Obsługa opcji "Change password"
        val changePasswordOption = findViewById<MaterialTextView>(R.id.tvChangePassword)
        changePasswordOption.setOnClickListener {
            // Przejście do aktywności zmiany hasła
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }

        // Obsługa opcji "Change language"
        val changeLanguageOption = findViewById<MaterialTextView>(R.id.tvChangeLanguage)
        changeLanguageOption.setOnClickListener {
            // Przejście do aktywności zmiany języka
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }
//
//        // Obsługa opcji "Change theme"
//        val changeThemeOption = findViewById<LinearLayout>(R.id.tvChangeTheme)
//        changeThemeOption.setOnClickListener {
//            // Przejście do aktywności zmiany motywu
//            val intent = Intent(this, ChangeThemeActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Obsługa opcji "App information"
//        val appInfoOption = findViewById<LinearLayout>(R.id.tvAppInformation)
//        appInfoOption.setOnClickListener {
//            // Przejście do aktywności z informacjami o aplikacji
//            val intent = Intent(this, AppInformationActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Obsługa opcji "Contact us"
//        val contactUsOption = findViewById<LinearLayout>(R.id.tvContactUs)
//        contactUsOption.setOnClickListener {
//            // Przejście do aktywności kontaktu z twórcami
//            val intent = Intent(this, ContactUsActivity::class.java)
//            startActivity(intent)
//        }
    }
}
