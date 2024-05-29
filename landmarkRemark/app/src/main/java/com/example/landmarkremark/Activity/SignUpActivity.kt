package com.example.landmarkremark.Activity

import android.content.ContentValues.TAG
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

class SignUpActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        db = FirebaseFirestore.getInstance()
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.passwordConfirmInput)
        val btnSignUp = findViewById<Button>(R.id.signUpButton)
        val btnLogin = findViewById<Button>(R.id.loginButton)
        val togglePasswordVisibilityButton = findViewById<ImageButton>(R.id.togglePasswordVisibilityButton)
        val togglePasswordVisibilityButton2 = findViewById<ImageButton>(R.id.togglePasswordVisibilityButton2)
        btnSignUp.isEnabled = false

        // Function to update the state of the login button
        // based on the input fields
        // If both fields are not empty, enable the button
        // Otherwise, disable the button
        fun updateButtonState() {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                btnSignUp.isEnabled = true
                btnSignUp.setBackgroundResource(R.drawable.bg_gradient5) // replace with your 'enabled' background
                btnSignUp.setTextColor(Color.WHITE) // replace with your 'enabled' text color
            } else {
                btnSignUp.isEnabled = false
                btnSignUp.setBackgroundResource(R.drawable.bg_gradient11) // replace with your 'disabled' background
                btnSignUp.setTextColor(Color.BLACK) // replace with your 'disabled' text color
            }
            btnSignUp.invalidate() // Force Android to redraw the button
        }

        // Function to create an account
        // If the account is created successfully, navigate to the login activity
        // If the account creation fails, display a toast message
        fun createAccount(user : User, loadingLayout: RelativeLayout) {
            // Add a new account user with a generated ID
            db.collection("users")
                .document(user.getId()!!)
                .set(user)
                .addOnSuccessListener {
                    loadingLayout.visibility = View.GONE // Hide the loading layout
                    Log.d(TAG, "DocumentSnapshot added with username: ${user.getUsername()}")
                    Toast.makeText(this@SignUpActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    loadingLayout.visibility = View.GONE
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this@SignUpActivity, "Registration failed", Toast.LENGTH_SHORT).show()
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

        // Toggle confirm password visibility
        togglePasswordVisibilityButton2.setOnClickListener {
            if (confirmPasswordInput.transformationMethod is PasswordTransformationMethod) {
                confirmPasswordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePasswordVisibilityButton2.setImageResource(R.drawable.ic_show_pass) // replace with your 'show password' icon
            } else {
                confirmPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordVisibilityButton2.setImageResource(R.drawable.ic_hide_pass) // replace with your 'hide password' icon
            }
        }

        // Check if the username already exists
        // If the username exists, display an error message
        // Otherwise, clear the error message
        usernameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString()
                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (!task.result!!.isEmpty) {
                                usernameInput.error = "Username already exists"
                                updateButtonState()
                            } else {
                                updateButtonState()
                                confirmPasswordInput.error = null
                            }
                        } else {
                            updateButtonState()
                            confirmPasswordInput.error = null
                        }
                    }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No need to implement
            }
        })

        // Check if password is at least 8 characters long
        // If the password is less than 8 characters, display an error message
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = passwordInput.text.toString()
                if (password.length < 8) {
                    updateButtonState()
                    passwordInput.error = "Password must be at least 8 characters long"
                } else {
                    updateButtonState()
                    passwordInput.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No need to implement
            }
        })

        // Check if password and confirm password match
        // If the passwords do not match confirm password, display an error message
        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = passwordInput.text.toString()
                val confirmPassword = s.toString()
                if (password != confirmPassword) {
                    updateButtonState()
                    confirmPasswordInput.error = "Passwords do not match"
                } else {
                    updateButtonState()
                    confirmPasswordInput.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No need to implement
            }
        })


        // handle click listeners to the buttons
        btnSignUp.setOnClickListener {
            val loadingLayout = findViewById<RelativeLayout>(R.id.loading_layout)
            loadingLayout.visibility = View.VISIBLE // Show loading layout
            val id = UUID.randomUUID().toString() // Generate a random ID
            val username = usernameInput.text.toString()
            val password = HashPassword().hashPassword(passwordInput.text.toString())// Encrypt password
            val user = User(id, username, password)
            createAccount(user, loadingLayout)// Create account
        }
        btnLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}