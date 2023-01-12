package com.autio.android_app.extensions

import android.location.Address
import android.location.Geocoder
import android.os.Build

/**
 * Returns an address if possible
 *
 * Returns null for safe operations e.g. fetching request with no internet connection
 */
fun Geocoder.getAddress(
    latitude: Double,
    longitude: Double,
    address: (Address?) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocation(latitude, longitude, 1) {address(it.firstOrNull())}
        return
    }

    try {
        address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
    } catch (e: Exception) {
        address(null)
    }
}