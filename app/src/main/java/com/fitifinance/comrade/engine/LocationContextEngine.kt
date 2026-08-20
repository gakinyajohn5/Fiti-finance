package com.fitifinance.comrade.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/** Drives dynamic dashboard theming based on detected surroundings. */
enum class ThemeMode { CAMPUS, BAR, KIBANDA }

/** A simplified POI used for geofence-style proximity checks (Google Places / OSM in production). */
data class ContextPoi(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val mode: ThemeMode
)

/**
 * Location-Aware Context Engine. In production this would register real
 * Android Geofences via GeofencingClient against Google Places/OSM POIs and
 * Wi-Fi SSID fingerprints for indoor venues (e.g. campus mess Wi-Fi vs bar
 * Wi-Fi). Here we implement the proximity-matching core plus a live
 * StateFlow the UI observes to switch themes and dashboard focus.
 */
class LocationContextEngine(private val context: Context) {

    private val _currentMode = MutableStateFlow(ThemeMode.CAMPUS)
    val currentMode: StateFlow<ThemeMode> = _currentMode

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Refreshes the theme mode by checking the last known location against
     * registered POIs. Falls back silently to the current mode if permission
     * isn't granted or location is unavailable.
     */
    suspend fun refresh(pois: List<ContextPoi>) {
        if (!hasLocationPermission()) return
        val location = runCatching { fusedLocationClient.lastLocation.await() }.getOrNull() ?: return

        val match = pois.firstOrNull { poi ->
            distanceMeters(location.latitude, location.longitude, poi.latitude, poi.longitude) <= poi.radiusMeters
        }
        _currentMode.value = match?.mode ?: ThemeMode.CAMPUS
    }

    /**
     * Manual override so the theme can be demoed on emulators/devices without
     * live geofence data, or toggled by the user from the dashboard.
     */
    fun forceMode(mode: ThemeMode) {
        _currentMode.value = mode
    }

    /** Impulse Buy Warning Geofences: high-spending zones like malls / fast-food hubs. */
    fun isImpulseZone(poi: ContextPoi): Boolean =
        poi.name.contains("mall", ignoreCase = true) || poi.name.contains("fast food", ignoreCase = true)

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}
