package com.manusha.taskflow

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    // Firebase services for authentication and database
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind UI elements from XML layout
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val cbAgree = findViewById<CheckBox>(R.id.cb_agree)
        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

        // Set signup button click listener
        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validate user input
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirm password check
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Terms and conditions check
            if (!cbAgree.isChecked) {
                Toast.makeText(this, "Please agree to terms", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Step 1: Check if username already exists in Firestore
            val lowerUsername = username.lowercase()
            db.collection("users").document(lowerUsername).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        // Step 2: Create user account in Firebase Auth
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    
                                    // Step 3: Save username mapping in Firestore for login
                                    val userData = hashMapOf("email" to email)
                                    db.collection("users").document(lowerUsername).set(userData)
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }

                                    // Update user profile with display name
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                    
                                    user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { profileTask ->
                                            // Redirect to main screen on success
                                            val intent = Intent(this, TodoActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                } else {
                                    // Handle Firebase Auth errors
                                    val errorMessage = task.exception?.message ?: "Unknown error"
                                    Toast.makeText(this, "Signup failed: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Connectivity/Database error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Return to login screen
        tvLogin.setOnClickListener {
            finish()
        }
    }
}
