package com.example.landmarkremark.Activity

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.landmarkremark.Enity.Notes
import com.example.landmarkremark.R
import com.example.landmarkremark.Service.CustomNote
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var keyword : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        keyword = intent.getStringExtra("keyword") //
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        val mapOptionButton : ImageButton = findViewById(R.id.mapOptionBtn)
        val backBtn : ImageButton = findViewById(R.id.back_btn)
        val textKeyword : TextView = findViewById(R.id.keyword_search)
        val popupMenu = PopupMenu(this, mapOptionButton)
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        textKeyword.text = getString(R.string.search_result_for, keyword)
        popupMenu.menuInflater.inflate(R.menu.map_option_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
                menuItem -> changeMapType(menuItem.itemId)
            true
        }
        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }
        backBtn.setOnClickListener{
            finish()
        }

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        setUpMap()
    }

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
        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("account", MODE_PRIVATE)
        val userIdSharedPre = sharedPreferences.getString("id", "") // Get the user id from shared preferences
        db.collection("notes")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val notes = snapshot.documents.map { document -> document.toObject(Notes::class.java) }
                    mGoogleMap?.clear()
                    for (note in notes) {
                        val latLng = LatLng(note?.getLat()!!, note?.getLong()!!)
                        val markerView = LayoutInflater.from(this).inflate(R.layout.note_item, null)
                        val noteUser: TextView = markerView.findViewById(R.id.note_user)
                        val noteContent: TextView = markerView.findViewById(R.id.note_content)
                        val userId = note.getUserId()
                        db.collection("users")
                            .document(userId!!)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val username = document.getString("username")
                                    // check the keyword string is a substring of the username
                                    //if yes, set the background of the marker to a different color "note_search_username_bg"
                                    if (username?.contains(keyword ?: "", ignoreCase = true) == true) {
                                        markerView.setBackgroundResource(R.drawable.note_search_username_bg)
                                    }
                                    if (userId == userIdSharedPre) {
                                        noteUser.text = "You"
                                        noteUser.setTextColor(Color.CYAN)
                                    } else {
                                        noteUser.text = username
                                    }
                                    noteContent.text = CustomNote().shortenNoteText(note)
                                    //check the keyword string is a substring of the content of the note
                                    //if yes, set the background of the marker to a different color "note_search_content_bg"
                                    if (noteContent.text?.contains(keyword ?: "", ignoreCase = true) == true) {
                                        markerView.setBackgroundResource(R.drawable.note_search_content_bg)
                                    }
                                    val markerIcon = CustomNote().getMarkerIconFromView(markerView)
                                    val marker = mGoogleMap?.addMarker(MarkerOptions().position(latLng).icon(markerIcon))
                                    marker?.tag = note
                                } else {
                                    Log.d(ContentValues.TAG, "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(ContentValues.TAG, "get failed with ", exception)
                            }
                    }
                    mGoogleMap?.setOnMarkerClickListener { marker ->
                        // Get the note data from the marker
                        val note = marker.tag as? Notes

                        if (note != null) {
                            // Create an intent to start the DetailNoteActivity
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
                    Log.d(ContentValues.TAG, "Current data: null")
                }
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Read failed.", e)
            }
    }


}