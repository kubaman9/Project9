package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class TaskDetailActivity : AppCompatActivity() {

    private lateinit var tvTaskName: TextView
    private lateinit var btnMarkComplete: Button
    private lateinit var btnCancel: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private lateinit var taskList: List<String>
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        tvTaskName = findViewById(R.id.tvTaskName)
        btnMarkComplete = findViewById(R.id.btnMarkComplete)
        btnCancel = findViewById(R.id.btnCancel)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        taskList = intent.getStringArrayListExtra("taskList") ?: emptyList()
        currentPosition = intent.getIntExtra("taskIndex", 0)

        displayCurrentTask()

        btnMarkComplete.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("completedTask", taskList[currentPosition])
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnPrevious.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                displayCurrentTask()
            }
        }

        btnNext.setOnClickListener {
            if (currentPosition < taskList.size - 1) {
                currentPosition++
                displayCurrentTask()
            }
        }
    }

    private fun displayCurrentTask() {
        tvTaskName.text = taskList[currentPosition]
    }
}