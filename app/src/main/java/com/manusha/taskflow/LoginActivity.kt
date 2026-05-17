package com.manusha.taskflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    // Firebase Authentication instance for login
    private lateinit var auth: FirebaseAuth
    // Firestore Database instance for user data mapping
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind UI elements from XML
        val etIdentifier = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvSignup = findViewById<TextView>(R.id.tv_signup)

        // Set login button click listener
        btnLogin.setOnClickListener {
            val input = etIdentifier.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate input fields
            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if input is email or username
            if (input.contains("@")) {
                signInWithEmail(input, password)
            } else {
                // If username, check Firestore for matching email
                val searchName = input.lowercase()
                db.collection("users").document(searchName).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val email = document.getString("email")
                            if (email != null) {
                                signInWithEmail(email, password)
                            } else {
                                Toast.makeText(this, "Internal error: No email found", Toast.LENGTH_SHORT).show()
                            }
                        } else if (searchName == "manusha") {
                            // Extra fix for developer account
                            signInWithEmail("manushaakilanka53@gmail.com", password)
                        } else {
                            Toast.makeText(this, "Username not found. Try your Email instead.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Navigate to Signup activity
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    /**
     * Authenticates user using email and password via Firebase Auth.
     */
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Navigate to Main Todo screen on success
                    val user = auth.currentUser
                    val username = user?.displayName
                    if (username != null) {
                        // Sync username mapping for future logins
                        val mapping = hashMapOf("email" to email)
                        db.collection("users").document(username.lowercase()).set(mapping)
                    }

                    val intent = Intent(this, TodoActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Show error message on failure
                    val errorMessage = task.exception?.message ?: "Check password or connection"
                    Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}
