package com.manusha.taskflow

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manusha.taskflow.adapters.TaskAdapter
import com.manusha.taskflow.models.Task

class TodoActivity : AppCompatActivity() {
    // Firebase and Adapter properties
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind UI components
        val rvTasks = findViewById<RecyclerView>(R.id.rv_tasks)
        val btnAdd = findViewById<Button>(R.id.btn_add_task)
        val cvProfile = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cv_profile_nav)

        // Setup RecyclerView with Adapter and LayoutManager
        adapter = TaskAdapter(taskList,
            onEditClick = { task -> showAddTaskDialog(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        // Set listeners for Add task and Profile navigation
        btnAdd.setOnClickListener { showAddTaskDialog(null) }
        cvProfile.setOnClickListener { startActivity(Intent(this, UserInfoActivity::class.java)) }

        // Initial task load from Firestore
        loadTasks()
    }

    /**
     * Loads tasks belonging to the current user from Firestore with real-time updates.
     */
    private fun loadTasks() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("TodoActivity", "Error loading tasks", e)
                    Toast.makeText(this, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    taskList.clear()
                    for (doc in snapshots) {
                        val task = doc.toObject(Task::class.java)
                        task.id = doc.id
                        taskList.add(task)
                    }
                    android.util.Log.d("TodoActivity", "Loaded ${taskList.size} tasks")
                    adapter.notifyDataSetChanged()
                }
            }
    }

    /**
     * Shows a dialog to add a new task or edit an existing one.
     */
    private fun showAddTaskDialog(task: Task?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_task)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etTitle = dialog.findViewById<EditText>(R.id.et_task_title)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        if (task != null) {
            etTitle.setText(task.title)
        }

        btnOk.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                if (task == null) {
                    saveTask(title)
                } else {
                    updateTask(task.id, title)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Saves a new task to Firestore.
     */
    private fun saveTask(title: String) {
        val userId = auth.currentUser?.uid ?: return
        val task = Task(title = title, userId = userId, timestamp = System.currentTimeMillis())
        db.collection("tasks").add(task)
            .addOnFailureListener { Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show() }
    }

    /**
     * Updates an existing task title in Firestore.
     */
    private fun updateTask(id: String, title: String) {
        db.collection("tasks").document(id).update("title", title)
            .addOnFailureListener { Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show() }
    }

    /**
     * Deletes a task from Firestore.
     */
    private fun deleteTask(task: Task) {
        db.collection("tasks").document(task.id).delete()
            .addOnFailureListener { Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show() }
    }
}
