package com.example.midtermproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LandingPage : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)
        val etEmail = findViewById<EditText>(R.id.etEmail) // EditText for email
        val btnProceed = findViewById<Button>(R.id.Proceed) // Button to proceed

        btnProceed.setOnClickListener {
            val email = etEmail.text.toString().trim() // Get email text

            if (email.isNotBlank()) {
                // Email is valid; pass to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_EMAIL", email) // Pass email as an extra
                startActivity(intent)
                finish() // Close LandingPageActivity
            } else {
            }
        }
    }

}