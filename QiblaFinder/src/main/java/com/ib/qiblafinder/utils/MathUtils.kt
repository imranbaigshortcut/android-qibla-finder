package com.ib.qiblafinder.utils

import kotlin.math.floor
import kotlin.math.roundToInt

object MatchUtils {
    fun closestAngle(angle1: Double): Double {
        return if (angle1 >= -180.0 && angle1 <= 180.0) angle1 else angle1 - (angle1 / 360.0).roundToInt() * 360L
    }

    fun normaliseWithBound(angle1: Double, angle2: Double): Double {
        return angle1 - angle2 * floor(angle1 / angle2)
    }

    fun unwindAngle(angle: Double): Double {
        return normaliseWithBound(angle, 360.0)
    }
}
