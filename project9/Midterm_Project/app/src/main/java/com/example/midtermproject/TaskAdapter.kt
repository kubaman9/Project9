package com.example.midtermproject

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val originalTasks: List<Task>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var filteredTasks: MutableList<Task> = originalTasks.toMutableList()

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTasks[position]
        holder.taskName.text = task.name
        holder.checkBox.isChecked = task.isCompleted

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = filteredTasks.size

    fun filter(query: String): List<Task> {
        notifyDataSetChanged()
        return originalTasks.filter { task ->
            task.name.contains(query, ignoreCase = true)
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        filteredTasks.clear()
        filteredTasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}