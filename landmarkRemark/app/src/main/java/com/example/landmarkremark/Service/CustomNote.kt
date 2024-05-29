package com.example.landmarkremark.Service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.example.landmarkremark.Enity.Notes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class CustomNote {
    // Function to get the marker icon from a view
    // Convert the view to a bitmap and return the bitmap descriptor
    fun getMarkerIconFromView(view: View): BitmapDescriptor {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED) // Measure the view
        view.measure(measureSpec, measureSpec)
        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888) // Create a bitmap
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // Function to shorten the note text
    fun shortenNoteText(note: Notes): String {
        val noteText = note.getNote()
        val words = noteText?.split(" ") // Split the note text into words
        if (words != null && words.size > 5) {
            return words.take(5).joinToString(" ") + "..." // Return the first 5 words + "..."
        }
        return noteText!! // Return the full note text if it has less than 5 words
    }
}