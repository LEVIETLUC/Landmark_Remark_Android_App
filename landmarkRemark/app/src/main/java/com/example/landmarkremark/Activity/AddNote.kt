package com.example.landmarkremark.Activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.landmarkremark.Enity.Notes
import com.example.landmarkremark.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.util.*

class AddNote : BottomSheetDialogFragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext()) // Get the location client
        val view = inflater.inflate(R.layout.add_note_layout, container, false)
        val noteContentEditText = view.findViewById<EditText>(R.id.inputNoteContent)
        val btnPost = view.findViewById<Button>(R.id.postButton)
        val btnCancel = view.findViewById<Button>(R.id.cancelButton)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnPost.setOnClickListener {
            val id = UUID.randomUUID().toString()
            var latLocation: Double? = null
            var longLocation: Double? = null
            val sharedPreferences = requireContext().getSharedPreferences("account", MODE_PRIVATE)
            val userId = sharedPreferences.getString("id", "")
            val noteContent = noteContentEditText.text.toString()
            val currentDateTime = LocalDateTime.now() // Get the current date and time

            // Check if the user has granted location permission
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Send data to server
                    latLocation = location.latitude
                    longLocation = location.longitude
                    Log.d(TAG, "latLocationString: $latLocation")
                    Log.d(TAG, "longLocationString: $longLocation")
                    if (userId != null && latLocation != null && longLocation != null) {
                        val note = Notes(id, currentDateTime.toString(), userId, noteContent, latLocation!!, longLocation!!)
                        addNoteAtCurentLocation(note) // Add the note at the current location
                    }
                }
            }
        }
        return view
    }

    // Function to add a note at the current location
    // If a note already exists at the same location, same userId with id user current
    // delete the existing note and add the new note
    // If no note exists at the location, add the new note
    private fun addNoteAtCurentLocation(note: Notes) {
        val db = FirebaseFirestore.getInstance()

        // Query the notes collection for a note with the same userId, lat, and long
        db.collection("notes")
            .whereEqualTo("userId", note.getUserId())
            .whereEqualTo("lat", note.getLat())
            .whereEqualTo("long", note.getLong())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Delete the existing note if found
                    db.collection("notes").document(document.id).delete()
                }
                // Add the new note
                db.collection("notes")
                    .document(note.getId()!!)
                    .set(note)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${note.getId()}")
                        Toast.makeText(requireActivity(), "Note added successfully", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding note", e)
                        Toast.makeText(requireActivity(), "Error adding note", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error querying notes", e)
                Toast.makeText(requireActivity(), "Error querying notes", Toast.LENGTH_SHORT).show()
            }
    }

}