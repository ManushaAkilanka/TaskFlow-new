package com.manusha.taskflow

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class UserInfoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private lateinit var ivAvatar: ImageView
    private lateinit var tvAvatarText: TextView
    private lateinit var ivEditAvatar: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            if (::ivEditAvatar.isInitialized) {
                Glide.with(this).load(it).into(ivEditAvatar)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val tvName = findViewById<TextView>(R.id.tv_user_name)
        val tvEmail = findViewById<TextView>(R.id.tv_user_email)
        ivAvatar = findViewById(R.id.iv_user_avatar)
        tvAvatarText = findViewById(R.id.tv_avatar_text)
        val btnSignOut = findViewById<Button>(R.id.btn_sign_out)
        val btnEditInfo = findViewById<Button>(R.id.btn_edit_info)
        val tvDevInfo = findViewById<TextView>(R.id.tv_dev_info_link)

        updateUI()

        btnSignOut.setOnClickListener { showSignOutDialog() }
        btnEditInfo.setOnClickListener { showEditProfileDialog() }
        tvDevInfo.setOnClickListener { startActivity(Intent(this, DevInfoActivity::class.java)) }
    }

    private fun updateUI() {
        val user = auth.currentUser
        val tvName = findViewById<TextView>(R.id.tv_user_name)
        val tvEmail = findViewById<TextView>(R.id.tv_user_email)

        tvName.text = user?.displayName ?: "No Name"
        tvEmail.text = user?.email ?: "No Email"

        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).into(ivAvatar)
            tvAvatarText.visibility = TextView.GONE
        } else {
            ivAvatar.setImageResource(android.R.drawable.progress_horizontal)
            tvAvatarText.text = user?.displayName?.take(1)?.uppercase() ?: "T"
            tvAvatarText.visibility = TextView.VISIBLE
        }
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_profile)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        ivEditAvatar = dialog.findViewById(R.id.iv_edit_avatar)
        val tvChangePhoto = dialog.findViewById<TextView>(R.id.tv_change_photo)
        val etUsername = dialog.findViewById<EditText>(R.id.et_edit_username)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val user = auth.currentUser
        etUsername.setText(user?.displayName)
        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).into(ivEditAvatar)
        }

        tvChangePhoto.setOnClickListener { pickImage.launch("image/*") }
        ivEditAvatar.setOnClickListener { pickImage.launch("image/*") }

        btnSave.setOnClickListener {
            val newName = etUsername.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(selectedImageUri)
                .build()

            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        updateUI()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showSignOutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_sign_out)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<Button>(R.id.btn_ok).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialog.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
