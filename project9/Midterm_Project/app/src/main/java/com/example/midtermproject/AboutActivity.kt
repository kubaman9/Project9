package com.example.midtermproject

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btnBack: Button = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}