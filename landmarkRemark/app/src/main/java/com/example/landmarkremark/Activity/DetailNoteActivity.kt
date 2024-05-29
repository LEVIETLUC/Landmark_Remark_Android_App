package com.example.landmarkremark.Activity

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.landmarkremark.Enity.Notes
import com.example.landmarkremark.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


class DetailNoteActivity : AppCompatActivity(), Serializable {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_note)
        supportActionBar?.hide() // hide the action bar
        val extras = intent.extras
        val note = extras?.getSerializable("note") as? Notes// get the note object from the intent
        val noteUser: TextView = findViewById(R.id.note_user)
        val noteContent: TextView = findViewById(R.id.note_content)
        val noteCreatedAt: TextView = findViewById(R.id.note_time_created)
        val noteLocation: TextView = findViewById(R.id.note_location_away)
        val closeBtn: ImageButton = findViewById(R.id.close_btn)
        val deleteBtn: Button = findViewById(R.id.delete_btn)
        val db = FirebaseFirestore.getInstance() // Get the firestore instance
        val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
        val userIdSharedPre = sharedPreferences.getString("id", "")
        val userId = note?.getUserId()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        closeBtn.setOnClickListener {
            finish()
        }

        // Check if the current user is the owner of the note
        // If so, show the delete button
        // Otherwise, hide the delete button
        if (userIdSharedPre == userId) {
            deleteBtn.visibility = View.VISIBLE
        } else {
            deleteBtn.visibility = View.INVISIBLE
        }
        // handle the delete button click event
        deleteBtn.setOnClickListener {
            finish()
            db.collection("notes")
                .document(note?.getId()!!)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error deleting document", e)
                }
        }


        // Get the user information from the database firestore
        db.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("username")
                    noteUser.text = username
                    noteContent.text = note.getNote()
                    noteCreatedAt.text = formatTime(note.getCreatedAt()!!)
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@addOnSuccessListener
                    }
                    fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                        if (location != null) {
                            val distance = calculateDistance(location.latitude, location.longitude, note.getLat()!!, note.getLong()!!)
                            noteLocation.text = getString(R.string.distance_away, distance)
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }


    }

    // Function to format the time to "yyyy-MM-dd 'at' hh:mm"
    private fun formatTime(time: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
        val targetFormat = SimpleDateFormat("yyyy-MM-dd 'at' hh:mm", Locale.US)

        val originalDate = time?.let { originalFormat.parse(it) }
        val formattedDate = originalDate?.let { targetFormat.format(it) }
        return formattedDate.toString()
    }

    // Function to calculate the distance between note location with current location using the Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        // Distance calculation algorithm using the Haversine formula
        val a = sin(latDistance / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

}