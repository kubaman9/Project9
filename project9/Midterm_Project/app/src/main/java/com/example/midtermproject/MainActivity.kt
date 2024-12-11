package com.example.midtermproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var btnAddTask: Button
    private lateinit var btnAbout: Button
    private lateinit var searchBar: EditText
    private val taskList = mutableListOf<Task>()
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = intent?.getStringExtra("USER_EMAIL") ?: ""


        fetchAndSaveTasks {
            // Now `taskList` is updated with the Firestore data
            Log.d("TaskList", "Loaded tasks: $taskList")
        }

        recyclerView = findViewById(R.id.recyclerView)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnAbout = findViewById(R.id.btnAbout)
        searchBar = findViewById(R.id.searchBar)

        taskAdapter = TaskAdapter(taskList) { position -> openTaskDetail(position) }
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchTasksFromFirestore(email)

        btnAddTask.setOnClickListener {
            if (taskList.size < 20) {
                val intent = Intent(this, AddTaskActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK)
            } else {
                btnAddTask.text = "Too much work!"
            }
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }

        taskAdapter.notifyDataSetChanged()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {taskAdapter.updateTasks(taskAdapter.filter(s.toString()))}
        })

    }

    private fun resetSearch() {
        searchBar.text.clear() // Clear the search bar
        taskAdapter.filter("") // Reset the filter in the adapter
    }

    private fun openTaskDetail(position: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putStringArrayListExtra("taskList", ArrayList(taskList.map { it.name }))
        intent.putExtra("taskIndex", position)
        startActivityForResult(intent, REQUEST_CODE_TASK_DETAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        taskAdapter.notifyDataSetChanged()
        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            val taskName = data?.getStringExtra("task") ?: return

            // Add task to Firestore under the user's email
            val db = Firebase.firestore
            val newTask = Task(taskName, isCompleted = false)

            db.collection("users")
                .document(email)
                .collection("tasks")
                .add(newTask)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Task added successfully for user: $email")

                    // Fetch tasks from Firestore to refresh the taskList
                    fetchTasksFromFirestore(email)
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error adding task for user: $email", e)
                }
        } else if (requestCode == REQUEST_CODE_TASK_DETAIL && resultCode == RESULT_OK) {
            val completedTaskName = data?.getStringExtra("completedTask") ?: return

            // Remove the task from Firestore
            val db = Firebase.firestore
            db.collection("users")
                .document(email)
                .collection("tasks")
                .whereEqualTo("name", completedTaskName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        db.collection("users")
                            .document(email)
                            .collection("tasks")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("MainActivity", "Task deleted successfully: $completedTaskName")

                                // Update local task list
                                val taskToRemove = taskList.find { it.name == completedTaskName }
                                taskToRemove?.let {
                                    taskList.remove(it)
                                    taskAdapter.notifyDataSetChanged()
                                    fetchTasksFromFirestore(email)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("MainActivity", "Error deleting task: $completedTaskName", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error finding task to delete: $completedTaskName", e)
                }
        }
    }

    private fun fetchTasksFromFirestore(email: String) {
        val db = Firebase.firestore
        val userTasksCollection = db
            .collection("users")
            .document(email)
            .collection("tasks")

        userTasksCollection.get()
            .addOnSuccessListener { querySnapshot ->
                taskList.clear() // Clear the existing task list
                for (document in querySnapshot) {
                    val task = document.toObject(Task::class.java)
                    taskList.add(task)
                }
                taskAdapter.updateTasks(taskList) // Refresh the UI
                Log.d("MainActivity", "Tasks loaded successfully for $email")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error loading tasks for $email: ${e.message}")
            }
    }



    private fun saveTasks() {
        val db = Firebase.firestore
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val data = gson.toJson(taskList)

        val userTasksCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(email)
            .collection("tasks")

        for (task in taskList) {
            val taskData = hashMapOf(
                "name" to task.name,
                "isCompleted" to task.isCompleted
            )

            // Use the task name as the document ID to ensure unique tasks
            userTasksCollection.document(task.name)
                .set(taskData)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Task '${task.name}' saved successfully under $email")
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error saving task '${task.name}': ${e.message}")
                }
        }

        editor.putString("taskList", data)
        editor.apply()
    }

    private fun fetchAndSaveTasks(onComplete: () -> Unit) {
        val db = Firebase.firestore
        val collectionName = "tasks"

        // Fetch tasks from Firestore
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { document ->
                    document.toObject(Task::class.java)
                }

                // Save tasks to SharedPreferences
                saveTasksToSharedPreferences(tasks)

                // Execute callback
                onComplete()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching tasks", exception)
                onComplete() // Proceed even if the fetch fails
            }
    }

    private fun saveTasksToSharedPreferences(tasks: List<Task>) {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(tasks)
        editor.putString("taskList", json)
        editor.apply()
        Log.d("SharedPreferences", "Tasks saved to SharedPreferences")
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("taskList", null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        taskList.clear()
        if (json != null) {
            taskList.addAll(gson.fromJson(json, type))
        }
    }

    companion object {
        const val REQUEST_CODE_ADD_TASK = 1
        const val REQUEST_CODE_TASK_DETAIL = 2
    }
}