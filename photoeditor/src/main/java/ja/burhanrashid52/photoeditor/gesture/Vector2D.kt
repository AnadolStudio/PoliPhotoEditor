package ja.burhanrashid52.photoeditor.gesture

import android.graphics.PointF
import kotlin.math.atan2
import kotlin.math.sqrt

class Vector2D : PointF() {

    companion object {

        fun getAngle(vector1: Vector2D, vector2: Vector2D): Float {
            vector1.normalize()
            vector2.normalize()
            val degrees = 180.0 / Math.PI * (atan2(vector2.y.toDouble(), vector2.x.toDouble()) - Math.atan2(vector1.y.toDouble(), vector1.x.toDouble()))

            return degrees.toFloat()
        }
    }

    private fun normalize() {
        val length = sqrt((x * x + y * y).toDouble()).toFloat()
        x /= length
        y /= length
    }
}
