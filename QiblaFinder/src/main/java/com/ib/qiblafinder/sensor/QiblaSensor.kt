package com.ib.qiblafinder.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ib.qiblafinder.utils.LocationCoordinates
import com.ib.qiblafinder.utils.QiblaAngle
import kotlin.math.roundToInt

class QiblaSensor(private val context: Context): SensorEventListener {

    var qiblaSensorEventListener: QiblaSensorEventListener? = null
    var currentLocation: LocationCoordinates? = null
    private  var  sensorManager: SensorManager?= null


    private val rotationVector = FloatArray(16)
    private val values = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent) {

        SensorManager.getRotationMatrixFromVector(rotationVector, event.values)
        SensorManager.getOrientation(rotationVector, this.values)
        val floatArray = values

        val intVal = (floatArray[1] * 57.29578f).toInt()

        var directionAngle =
            Math.toDegrees(floatArray[0].toDouble()).toFloat()

        qiblaSensorEventListener?.onDeviceAngle(intVal)


        if (0.0f.roundToInt() == directionAngle.roundToInt())  {
            return
        }

        directionAngle = -directionAngle

        setDirectionRotation(directionAngle.toDouble())
        qiblaSensorEventListener?.setDialRotation(directionAngle)

    }

    private fun setDirectionRotation(angle: Double) {
        try {
            if( currentLocation!= null) {
                val qiblaAngle = QiblaAngle(currentLocation!!)
                val stringBuilder = StringBuilder()

                stringBuilder.append("+")
                stringBuilder.append(qiblaAngle.angleDirection.roundToInt())
                val str = stringBuilder.toString()
                val currentDegree = angle.toFloat() + str.toFloat()
                qiblaSensorEventListener?.setDirectionRotation(currentDegree)
            }
            else {

            }
        }
        catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    fun register(eventListener: QiblaSensorEventListener) {
        qiblaSensorEventListener = eventListener
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI)
    }

    fun unregister() {
        sensorManager?.unregisterListener(this)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

interface QiblaSensorEventListener {
    fun onDeviceAngle(angle : Int)
    fun setDirectionRotation(angle: Float)
    fun setDialRotation(angle: Float)
}