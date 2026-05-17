package com.manusha.taskflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.view.View>(R.id.logo)
        val title = findViewById<android.view.View>(R.id.title)
        val subtitle = findViewById<android.view.View>(R.id.subtitle)

        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.splash_fade_scale)
        logo.startAnimation(animation)
        title.startAnimation(animation)
        subtitle.startAnimation(animation)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is signed in, go to TodoActivity (to be implemented)
                startActivity(Intent(this, TodoActivity::class.java))
            } else {
                // No user is signed in, go to LoginActivity (to be implemented)
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
