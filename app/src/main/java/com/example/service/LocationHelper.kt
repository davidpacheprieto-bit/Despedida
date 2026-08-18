package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UserCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 5.0f,
    val isSimulated: Boolean = false,
    val hasRealGpsFix: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

interface LocationTrackerHandle {
    fun stop()
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationProviderEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Calculate distance in meters using Haversine formula
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Calculate bearing in degrees from user to target
    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        val brng = Math.toDegrees(atan2(y, x))
        return ((brng + 360) % 360).toFloat()
    }

    // Format distance nicely: "45 m" or "1.2 km"
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000.0)
        }
    }

    /**
     * Starts continuous real-time GPS tracking using Google Play Services Fused Location
     * with Android LocationManager as backup.
     */
    @SuppressLint("MissingPermission")
    fun startRealtimeLocationUpdates(
        context: Context,
        onLocationReceived: (UserCoordinates) -> Unit
    ): LocationTrackerHandle {
        if (!hasLocationPermission(context)) {
            return object : LocationTrackerHandle { override fun stop() {} }
        }

        val appContext = context.applicationContext
        val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        var lastProcessedTimestamp = 0L

        fun processLocation(location: Location?, source: String) {
            if (location == null) return
            val now = System.currentTimeMillis()
            // Avoid flooding updates within 400ms unless high accuracy
            if (now - lastProcessedTimestamp < 400L && location.accuracy > 20f) return
            lastProcessedTimestamp = now

            val coords = UserCoordinates(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                isSimulated = false,
                hasRealGpsFix = true,
                timestamp = location.time
            )
            onLocationReceived(coords)
        }

        // 1. Immediate Last Known Location check
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) processLocation(loc, "fused_last")
            }
        } catch (e: Exception) {
            // ignore
        }

        if (locationManager != null) {
            try {
                val gpsLast = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLast != null) processLocation(gpsLast, "gps_last")
                val netLast = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (netLast != null) processLocation(netLast, "net_last")
            } catch (e: Exception) {
                // ignore
            }
        }

        // 2. Continuous Fused Location updates
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(0.5f)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    processLocation(loc, "fused_continuous")
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // ignore
        }

        // 3. Fallback standard LocationListener
        val systemLocationListener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                processLocation(loc, "system_lm")
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (locationManager != null) {
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        1f,
                        systemLocationListener,
                        Looper.getMainLooper()
                    )
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        2f,
                        systemLocationListener,
                        Looper.getMainLooper()
                    )
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        return object : LocationTrackerHandle {
            override fun stop() {
                try {
                    fusedClient.removeLocationUpdates(locationCallback)
                } catch (e: Exception) {
                    // ignore
                }
                try {
                    locationManager?.removeUpdates(systemLocationListener)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestSingleFreshLocation(
        context: Context,
        onLocationReceived: (UserCoordinates) -> Unit,
        onError: () -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError()
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        onLocationReceived(
                            UserCoordinates(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                accuracy = loc.accuracy,
                                isSimulated = false,
                                hasRealGpsFix = true
                            )
                        )
                    } else {
                        // Try fallback to last known
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onLocationReceived(
                                    UserCoordinates(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        accuracy = lastLoc.accuracy,
                                        isSimulated = false,
                                        hasRealGpsFix = true
                                    )
                                )
                            } else {
                                onError()
                            }
                        }.addOnFailureListener { onError() }
                    }
                }
                .addOnFailureListener {
                    onError()
                }
        } catch (e: Exception) {
            onError()
        }
    }
}
