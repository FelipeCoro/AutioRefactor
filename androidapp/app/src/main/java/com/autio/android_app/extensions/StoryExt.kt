package com.autio.android_app.extensions

import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun List<Story>.findNearestToCoordinates(coordinates: LatLng): Story? {
    return minByOrNull {
        abs(
            sqrt(
                (coordinates.latitude - it.lat).pow(2) +
                        (coordinates.longitude - it.lng).pow(2)
            )
        )
    }
}
