package com.manusha.taskflow

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DevInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_info)

        findViewById<Button>(R.id.btn_exit).setOnClickListener {
            finish()
        }
    }
}
