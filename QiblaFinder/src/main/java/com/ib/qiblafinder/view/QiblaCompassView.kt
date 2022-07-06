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
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ib.qiblafinder.QiblaDegreeListener
import com.ib.qiblafinder.R
import kotlin.math.roundToInt

class QiblaCompassView : FrameLayout, SensorEventListener {

    private var currentDegree = 0f
    private  var  mSensorManager: SensorManager?= null
    private var rotationDegree: Float = 138.0f
    private var currentLocation = Location("current location")

    private lateinit var imageNeedle: ImageView
    private lateinit var imageDial: ImageView
    private lateinit var textViewStatus: TextView

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

        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        imageNeedle.rotation = rotationDegree
        imageNeedle.refreshDrawableState()

        mSensorManager?.registerListener(this, mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI)

        if(latitude !=0f && longitude !=0f) {
            currentLocation.apply {
                this.latitude = latitude.toDouble()
                this.longitude = longitude.toDouble()
            }
        }

        invalidateUI()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSensorManager?.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun invalidateUI () {

        if(currentLocation.latitude !=0.0 && currentLocation.longitude !=0.0) {
            val destinationLoc = Location("service Provider")

            //Latitude and Longitude of Kaaba
            destinationLoc.latitude = 21.422487
            destinationLoc.longitude = 39.826206

            val bearTo = currentLocation.bearingTo(destinationLoc)

            rotationDegree = if (bearTo < 0) {
                bearTo + 360
            } else
                bearTo
        }

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

        val degree = event.values[0].roundToInt().toFloat()

        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )

        rotateAnimation.duration = 300
        rotateAnimation.fillAfter = true

        imageNeedle.startAnimation(rotateAnimation)
        imageDial.startAnimation(rotateAnimation)
        currentDegree = -degree

        degreeListener?.onDegreeChange(currentDegree)

    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
    }
}