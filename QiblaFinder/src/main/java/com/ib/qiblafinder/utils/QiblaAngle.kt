package com.ib.qiblafinder.utils


import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


data class LocationCoordinates(val latitude: Double, val longitude: Double)

class QiblaAngle(location: LocationCoordinates) {

    companion object {
        val MAKKAH = LocationCoordinates(21.4225241, 39.8261818)
    }

    val angleDirection = calculateQiblaDirection(location)

    private fun calculateQiblaDirection(paramCoordinates: LocationCoordinates): Double {
        val (latitude, longitude) = MAKKAH
        val deltaLong = Math.toRadians(longitude) - Math.toRadians(paramCoordinates.longitude)
        val deltaLat = Math.toRadians(paramCoordinates.latitude)

        return MatchUtils.unwindAngle(
            Math.toDegrees(
                atan2(
                    sin(deltaLong), cos(deltaLat) * tan(
                        Math.toRadians(
                            latitude
                        )
                    ) - sin(deltaLat) * cos(deltaLong)
                )
            )
        )
    }
}