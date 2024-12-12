package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {

    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var etTaskName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
        etTaskName = findViewById(R.id.etTaskName)

        btnSave.setOnClickListener {
            val taskName = etTaskName.text.toString()
            if (taskName.isNotBlank()) {
                val resultIntent = Intent()
                resultIntent.putExtra("task", taskName)
                setResult(RESULT_OK, resultIntent)//pass name into main activity to add it to firestore
                finish()
            }
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}