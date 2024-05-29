package com.example.landmarkremark.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.landmarkremark.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val thread = Thread {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
//                get SharedPreferences object with name "account"
//                and check if id, username and password in sharedPreferences are valid or not?
//                If so, move to MainActivity
//                Otherwise, LoginActivity
                val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
                val id = sharedPreferences.getString("id", null)
                val username = sharedPreferences.getString("username", null)
                val password = sharedPreferences.getString("password", null)
                val intent = if (id != null && username != null && password != null) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }

        thread.start()
    }
}