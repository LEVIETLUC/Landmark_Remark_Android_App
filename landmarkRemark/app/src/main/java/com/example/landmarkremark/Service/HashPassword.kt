package com.example.landmarkremark.Service
import java.math.BigInteger
import java.security.MessageDigest
class HashPassword {

    // Function to hash the password using SHA-256 algorithm
    fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(password.toByteArray())
        return BigInteger(1, messageDigest.digest()).toString(16).padStart(32, '0')
    }
}