package com.example.thesisapp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val etEmail: EditText = findViewById(R.id.etRegisterEmail)
        val etPassword: EditText = findViewById(R.id.etRegisterPassword)
        val btnRegister: Button = findViewById(R.id.btnRegister)
        val tvLogin: TextView = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Implementacja logiki rejestracji
                Toast.makeText(this, "Registered: $email", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}