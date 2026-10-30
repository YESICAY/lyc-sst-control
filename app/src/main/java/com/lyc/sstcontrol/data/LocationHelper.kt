package com.lyc.sstcontrol.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrent(onResult: (GpsPoint?) -> Unit) {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setDurationMillis(8000)
            .setMaxUpdateAgeMillis(60_000)
            .build()
        client.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) onResult(location.toPoint()) else fallbackToLast(onResult)
            }
            .addOnFailureListener { fallbackToLast(onResult) }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToLast(onResult: (GpsPoint?) -> Unit) {
        client.lastLocation
            .addOnSuccessListener { onResult(it?.toPoint()) }
            .addOnFailureListener { onResult(null) }
    }

    private fun Location.toPoint() = GpsPoint(latitude, longitude, accuracy.toDouble())
}