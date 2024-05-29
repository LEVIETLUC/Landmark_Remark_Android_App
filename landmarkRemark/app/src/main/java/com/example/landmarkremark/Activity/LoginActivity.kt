package com.example.landmarkremark.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.landmarkremark.Enity.User
import com.example.landmarkremark.R
import com.example.landmarkremark.Service.HashPassword
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : AppCompatActivity(){
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        db = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val togglePasswordVisibilityButton = findViewById<ImageButton>(R.id.togglePasswordVisibilityButton)
        val btnSignUp = findViewById<Button>(R.id.signUpButton)
        val btnLogin = findViewById<Button>(R.id.loginButton)
        btnLogin.isEnabled = false

        // Function to update the state of the login button
        // based on the input fields
        // If both fields are not empty, enable the button
        // Otherwise, disable the button
        fun updateButtonState() {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                btnLogin.isEnabled = true
                btnLogin.setBackgroundResource(R.drawable.bg_gradient5) // replace with your 'enabled' background
                btnLogin.setTextColor(Color.WHITE) // replace with your 'enabled' text color
            } else {
                btnLogin.isEnabled = false
                btnLogin.setBackgroundResource(R.drawable.bg_gradient11) // replace with your 'disabled' background
                btnLogin.setTextColor(Color.BLACK) // replace with your 'disabled' text color
            }
            btnLogin.invalidate() // Force Android to redraw the button
        }

        // Function to check if the user exists in the database
        // If the user exists, save the user's information in SharedPreferences
        // and navigate to the main activity
        // If the user does not exist or incorrect password, username and display toast
        fun checkAccount(user: User, loadingLayout: RelativeLayout) {
            // Query the database to check if the user exists in firestore
            db.collection("users")
                .whereEqualTo("username", user.getUsername())
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (!documents.isEmpty) {
                            val dbUser = documents.documents[0].toObject(User::class.java)
                            if (dbUser != null) {
                                // Check if the password matches the password in the database
                                if (dbUser.getPassword() == user.getPassword()) {
                                    loadingLayout.visibility = View.GONE//hide loading layout
                                    val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("id", dbUser.getId())
                                    editor.putString("username", dbUser.getUsername())
                                    editor.putString("password", dbUser.getPassword())
                                    editor.apply()
                                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    // Navigate to next activity
                                } else {
                                    // Passwords do not match
                                    loadingLayout.visibility = View.GONE
                                    Toast.makeText(this@LoginActivity, "Invalid password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // User not found
                            loadingLayout.visibility = View.GONE
                            Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Query failed
                        loadingLayout.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Toggle password visibility
        togglePasswordVisibilityButton.setOnClickListener {
            if (passwordInput.transformationMethod is PasswordTransformationMethod) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePasswordVisibilityButton.setImageResource(R.drawable.ic_show_pass) // replace with your 'show password' icon
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordVisibilityButton.setImageResource(R.drawable.ic_hide_pass) // replace with your 'hide password' icon
            }
        }

        // Add text username change listeners to the input fields
        usernameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Update the state of the login button
                updateButtonState()
            }
        })

        // Add text password change listeners to the input fields
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Update the state of the login button
                updateButtonState()
            }
        })

        // handle click listeners to the buttons
        btnLogin.setOnClickListener {
            val loadingLayout = findViewById<RelativeLayout>(R.id.loading_layout)
            loadingLayout.visibility = View.VISIBLE //show loading layout
            val username = usernameInput.text.toString()
            val password = HashPassword().hashPassword(passwordInput.text.toString())//decrypt password
            val user = User(username, password)
            checkAccount(user, loadingLayout)//check account
        }
        btnSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }}