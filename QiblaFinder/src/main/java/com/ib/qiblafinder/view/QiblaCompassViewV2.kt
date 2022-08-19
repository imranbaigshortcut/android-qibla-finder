package com.ib.qiblafinder.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ib.qiblafinder.QiblaDegreeListener
import com.ib.qiblafinder.R
import com.ib.qiblafinder.utils.LocationCoordinates
import com.ib.qiblafinder.utils.QiblaAngle
import kotlin.math.roundToInt

class QiblaCompassViewV2 : FrameLayout, SensorEventListener {

    private val rotationVector = FloatArray(16)
    private val values = FloatArray(3)

    private var currentDegree = 0f
    private  var  mSensorManager: SensorManager?= null
    private var rotationDegree: Float = 138.0f
    private var currentLocation = Location("current location")

    private lateinit var imageNeedle: ImageView
    private lateinit var imageDial: ImageView
    private lateinit var textViewStatus: TextView
    private lateinit var line: View


    private var dialDrawable: Drawable? = null
    private var needleDrawable: Drawable? = null
    private var hideStatusText = false

    var degreeListener: QiblaDegreeListener? = null

    var location: Location
        get() = currentLocation
        set(value) {
            currentLocation = value
            invalidateUI()
        }

    var degree: Float
        get() = currentDegree
        set(value) {
            currentDegree = value
            invalidateUI()
        }


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.QiblaCompassView, defStyle, 0)

            currentDegree = typedArray.getFloat(
                R.styleable.QiblaCompassView_degrees, 0f)
            val latitude = typedArray.getFloat(
                R.styleable.QiblaCompassView_currentLatitude, 0f)
            val longitude = typedArray.getFloat(
                R.styleable.QiblaCompassView_currentLongitude, 0f)

        hideStatusText = typedArray.getBoolean(
            R.styleable.QiblaCompassView_hideStatusText, false)

        if (typedArray.hasValue(R.styleable.QiblaCompassView_dialDrawable)) {
            dialDrawable = typedArray.getDrawable(
                R.styleable.QiblaCompassView_dialDrawable
            )
            dialDrawable?.callback = this
        } else {
            dialDrawable = resources.getDrawable(R.drawable.ic_def_compass)
        }
        if (typedArray.hasValue(R.styleable.QiblaCompassView_needleDrawable)) {
            needleDrawable = typedArray.getDrawable(
                R.styleable.QiblaCompassView_needleDrawable
            )
            needleDrawable?.callback = this
        }
        else {
            needleDrawable = resources.getDrawable(R.drawable.ic_def_needle)
        }

        typedArray.recycle()

        val root = inflate(context, R.layout.view_qibla, this)

        imageNeedle = root.findViewById(R.id.imageNeedle)
        imageDial = root.findViewById(R.id.imageDial)
        textViewStatus = root.findViewById(R.id.textViewStatus)
        line = root.findViewById(R.id.line)


        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        imageNeedle.rotation = rotationDegree
        imageNeedle.refreshDrawableState()

        mSensorManager?.registerListener(this, mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI)

        if(latitude !=0f && longitude !=0f) {
            currentLocation.apply {
                this.latitude = latitude.toDouble()
                this.longitude = longitude.toDouble()
            }
        }

        invalidateUI()

        if(currentDegree != 0f) {
            setDirectionRotation(currentDegree.toDouble())
            setDialRotation(currentDegree)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSensorManager?.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun invalidateUI () {

        textViewStatus.text = "Latitude: ${currentLocation.latitude}, Longitude: ${currentLocation.longitude}, Degree: $rotationDegree"
        imageNeedle.rotation = rotationDegree

        dialDrawable?.let {
            imageDial.setImageDrawable(dialDrawable)
        }

        needleDrawable?.let {
            imageNeedle.setImageDrawable(needleDrawable)
        }

        textViewStatus.visibility = if(hideStatusText) View.GONE  else View.VISIBLE
    }

    override fun onSensorChanged(event: SensorEvent) {

        SensorManager.getRotationMatrixFromVector(rotationVector, event.values)
        SensorManager.getOrientation(rotationVector, this.values)
        val floatArray = values

        val intVal = (floatArray[1] * 57.29578f).toInt()

        var directionAngle =
            Math.toDegrees(floatArray[0].toDouble()).toFloat()

        if (intVal <= -35 || intVal >= 35) {
            line.visibility = View.VISIBLE
        } else {
            line.visibility = View.VISIBLE
        }

        if (0.0f.roundToInt() == directionAngle.roundToInt())  {
            return
        }


        directionAngle = -directionAngle

        setDirectionRotation(directionAngle.toDouble())
        setDialRotation(directionAngle)

        degreeListener?.onDegreeChange(currentDegree)

    }


    private fun setDirectionRotation(angle: Double) {
        try {
            val locationCoordinates = LocationCoordinates(this.currentLocation.latitude, this.currentLocation.longitude)
            val qibla = QiblaAngle(locationCoordinates)
            val stringBuilder = StringBuilder()

            stringBuilder.append("+")
            stringBuilder.append(qibla.angleDirection.roundToInt())
            val str = stringBuilder.toString()
            currentDegree = angle.toFloat() + str.toFloat()
            rotationDegree = currentDegree
            this.imageNeedle.rotation = currentDegree
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun setDialRotation(angle: Float) {
        try {
            imageDial.rotation = angle
        } catch ( exception: Exception) {
         degreeListener?.onDegreeChange(currentDegree)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {}
}

