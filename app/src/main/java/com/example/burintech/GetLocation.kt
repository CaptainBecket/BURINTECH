package com.example.burintech

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
fun getLocation(
    context: Context,
    callback: (lat: Double?, lon: Double?, error: String?) -> Unit
){
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // 1. Пытаемся получить последнее известное местоположение (быстро)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location.latitude, location.longitude, null)
            } else {
                // 2. Если последнего местоположения нет, запрашиваем обновление
                val locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    numUpdates = 1 // Только одно обновление
                    interval = 0 // Немедленный запрос
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val loc = locationResult.lastLocation
                        if (loc != null) {
                            callback(loc.latitude, loc.longitude, null)
                        } else {
                            callback(null, null, "Failed to get location")
                        }
                        fusedLocationClient.removeLocationUpdates(this) // Отписываемся
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
        .addOnFailureListener { e ->
            callback(null, null, "Error: ${e.message}")
        }



}
