package com.example.landmarkremark.Activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.landmarkremark.Enity.Notes
import com.example.landmarkremark.Enity.User
import com.example.landmarkremark.R
import com.example.landmarkremark.Service.CustomNote
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment // get the map fragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapOptionButton : ImageButton = findViewById(R.id.mapOptionBtn)
        val popupMenu = PopupMenu(this, mapOptionButton)
        val searchInput: EditText = findViewById(R.id.search_input)
        val addNoteButton: ImageButton = findViewById(R.id.addNoteBtn)
        val logoutButton: ImageButton = findViewById(R.id.logoutBtn)
        popupMenu.menuInflater.inflate(R.menu.map_option_menu, popupMenu.menu) // inflate the menu
        //handle map type change
        popupMenu.setOnMenuItemClickListener {
            menuItem -> changeMapType(menuItem.itemId)
            true
        }
        //handle map option button to show popup menu
        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }
        //handle addNoteBtn to show bottom sheet dialog
        addNoteButton.setOnClickListener {
            val addNoteBottomSheetDialogFragment = AddNote()
            addNoteBottomSheetDialogFragment.show(supportFragmentManager, "addNoteBottomSheetDialog")
        }
        //handle logout button show alert dialog and remove shared preferences before logout
        logoutButton.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you are logged out of this account?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Logout", null)
                .show()
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
                val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove("username")
                editor.remove("id")
                editor.remove("password")
                editor.clear()
                editor.apply()

                // Navigate the user back to the login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }

        //handle search input
        searchInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Perform the search when the user presses the check mark on the keyboard
                // Change to the SearchActivity with the search keyword string
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("keyword", v.text.toString())
                startActivity(intent)
                searchInput.text.clear()
                searchInput.clearFocus()
                true
            } else {
                false
            }
        }
    }

    // init map
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN //set mapp default
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true //enable zoom control
        setUpMap()


    }

    //handle map type change
    private fun changeMapType(mapType: Int) {
        when(mapType) {
            R.id.mapTypeNormal ->
                mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.mapTypeHybrid ->
                mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.mapTypeSatellite ->
                mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.mapTypeTerrain ->
                mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN

        }
    }

    //set up map
    private fun setUpMap() {
        // Check the location permission before getting the device location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        // Get the current location of the device and set the position of the map.
        mGoogleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

            }
        }
        // Get the notes from the Firestore database and add them to the map as markers.
        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
        val userIdSharedPre = sharedPreferences.getString("id", "")
        db.collection("notes")
            .addSnapshotListener { snapshot, e ->//listen to the notes collection changes
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val notes = snapshot.documents.map { document -> document.toObject(Notes::class.java) }
                    mGoogleMap?.clear() //when the notes collection changes, clear the map and re-initialize map
                    for (note in notes) {
                        val latLng = LatLng(note?.getLat()!!, note?.getLong()!!) //init latLng of note
                        val markerView = LayoutInflater.from(this).inflate(R.layout.note_item, null) //inflate the note_item layout
                        val noteUser: TextView = markerView.findViewById(R.id.note_user)
                        val noteContent: TextView = markerView.findViewById(R.id.note_content)
                        val userId = note.getUserId()
                        //check with the current user id to change to set noteUser to "You"
                        db.collection("users")
                            .document(userId!!)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val username = document.getString("username")
                                    if (userId == userIdSharedPre) {
                                        noteUser.text = "You"
                                        noteUser.setTextColor(Color.CYAN)
                                    } else {
                                        noteUser.text = username
                                    }
                                    // handle with shortenNoteText function
                                    //For sentences larger than 5 words, the 6th word onwards will be replaced with "..."
                                    noteContent.text = CustomNote().shortenNoteText(note)
                                    //get marker icon from the note_item layout
                                    val markerIcon = CustomNote().getMarkerIconFromView(markerView)
                                    // add marker to the map
                                    val marker = mGoogleMap?.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                                    //set note object to marker tag
                                    marker?.tag = note
                                } else {
                                    Log.d(TAG, "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "get failed with ", exception)
                            }
                    }
                    // handle marker click event
                    mGoogleMap?.setOnMarkerClickListener { marker ->
                        // Get the note data from the marker
                        val note = marker.tag as? Notes
                        if (note != null) {
                            val intent = Intent(this, DetailNoteActivity::class.java)
                            // Put the note data into the intent extras
                            intent.putExtra("note", note)
                            startActivity(intent)
                        }

                        // Return false to indicate that we have not consumed the event and that we wish
                        // for the default behavior to occur (which is for the camera to move such that the
                        // marker is centered and for the marker's info window to open, if it has one).
                        false
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

}