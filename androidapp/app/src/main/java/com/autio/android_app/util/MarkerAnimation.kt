package com.autio.android_app.util

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Property
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.lang.Math.*
import kotlin.math.pow


class MarkerAnimation {
    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition: LatLng =
            marker.position
        val handler =
            Handler(
                Looper.getMainLooper()
            )
        val start: Long =
            SystemClock.uptimeMillis()
        val interpolator: Interpolator =
            AccelerateDecelerateInterpolator()
        val durationInMs =
            3000f
        handler.post(
            object :
                Runnable {
                var elapsed: Long =
                    0
                var t =
                    0f
                var v =
                    0f

                override fun run() {
                    // Calculate progress using interpolator
                    elapsed =
                        SystemClock.uptimeMillis() - start
                    t =
                        elapsed / durationInMs
                    v =
                        interpolator.getInterpolation(
                            t
                        )
                    marker.position = latLngInterpolator.interpolate(
                        v,
                        startPosition,
                        finalPosition
                    )

                    // Repeat till progress is complete.
                    if (t < 1) {
                        // Post again 16ms later.
                        handler.postDelayed(
                            this,
                            16
                        )
                    }
                }
            })
    }

    fun animateMarkerToHC(
        marker: Marker,
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition: LatLng =
            marker.position
        val valueAnimator =
            ValueAnimator()
        valueAnimator.addUpdateListener { animation ->
            val v =
                animation.animatedFraction
            val newPosition: LatLng =
                latLngInterpolator.interpolate(
                    v,
                    startPosition,
                    finalPosition
                )
            marker.position = newPosition
        }
        valueAnimator.setFloatValues(
            0f,
            1f
        ) // Ignored.
        valueAnimator.duration =
            3000
        valueAnimator.start()
    }

    fun animateMarkerToICS(
        marker: Marker?,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val typeEvaluator: TypeEvaluator<LatLng> =
            TypeEvaluator<LatLng> { fraction, startValue, endValue ->
                latLngInterpolator.interpolate(
                    fraction,
                    startValue,
                    endValue
                )
            }
        val property: Property<Marker, LatLng> =
            Property.of(
                Marker::class.java,
                LatLng::class.java,
                "position"
            )
        val animator: ObjectAnimator =
            ObjectAnimator.ofObject(
                marker,
                property,
                typeEvaluator,
                finalPosition
            )
        animator.duration =
            3000
        animator.start()
    }
}

interface LatLngInterpolator {
    fun interpolate(
        fraction: Float,
        a: LatLng,
        b: LatLng
    ): LatLng

    class Linear :
        LatLngInterpolator {
        override fun interpolate(
            fraction: Float,
            a: LatLng,
            b: LatLng
        ): LatLng {
            val lat =
                (b.latitude - a.latitude) * fraction + a.latitude
            val lng =
                (b.longitude - a.longitude) * fraction + a.longitude
            return LatLng(
                lat,
                lng
            )
        }
    }

    class LinearFixed :
        LatLngInterpolator {
        override fun interpolate(
            fraction: Float,
            a: LatLng,
            b: LatLng
        ): LatLng {
            val lat =
                (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta =
                b.longitude - a.longitude

            // Take the shortest path across the 180th meridian.
            if (Math.abs(
                    lngDelta
                ) > 180
            ) {
                lngDelta -= Math.signum(
                    lngDelta
                ) * 360
            }
            val lng =
                lngDelta * fraction + a.longitude
            return LatLng(
                lat,
                lng
            )
        }
    }

    class Spherical :
        LatLngInterpolator {
        /* From github.com/googlemaps/android-maps-utils */
        override fun interpolate(
            fraction: Float,
            from: LatLng,
            to: LatLng
        ): LatLng {
            // http://en.wikipedia.org/wiki/Slerp
            val fromLat: Double =
                toRadians(
                    from.latitude
                )
            val fromLng: Double =
                toRadians(
                    from.longitude
                )
            val toLat: Double =
                toRadians(
                    to.latitude
                )
            val toLng: Double =
                toRadians(
                    to.longitude
                )
            val cosFromLat: Double =
                cos(fromLat)
            val cosToLat: Double =
                cos(toLat)

            // Computes Spherical interpolation coefficients.
            val angle =
                computeAngleBetween(
                    fromLat,
                    fromLng,
                    toLat,
                    toLng
                )
            val sinAngle: Double =
                sin(angle)
            if (sinAngle < 1E-6) {
                return from
            }
            val a: Double =
                sin((1 - fraction) * angle) / sinAngle
            val b: Double =
                sin(fraction * angle) / sinAngle

            // Converts from polar to vector and interpolate.
            val x: Double =
                a * cosFromLat * cos(
                    fromLng
                ) + b * cosToLat * cos(
                    toLng
                )
            val y: Double =
                a * cosFromLat * sin(
                    fromLng
                ) + b * cosToLat * sin(
                    toLng
                )
            val z: Double =
                a * sin(
                    fromLat
                ) + b * sin(
                    toLat
                )

            // Converts interpolated vector back to polar.
            val lat: Double =
                atan2(
                    z,
                    sqrt(
                        x * x + y * y
                    )
                )
            val lng: Double =
                atan2(
                    y,
                    x
                )
            return LatLng(
                toDegrees(
                    lat
                ),
                toDegrees(
                    lng
                )
            )
        }

        private fun computeAngleBetween(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double
        ): Double {
            // Haversine's formula
            val dLat =
                fromLat - toLat
            val dLng =
                fromLng - toLng
            return 2 * kotlin.math.asin(
                kotlin.math.sqrt(
                    kotlin.math.sin(
                        dLat / 2
                    )
                        .pow(
                            2.0
                        ) +
                            kotlin.math.cos(
                                fromLat
                            ) * kotlin.math.cos(
                        toLat
                    ) * kotlin.math.sin(
                        dLng / 2
                    )
                        .pow(
                            2
                        )
                )
            )
        }
    }
}