package com.ib.qiblafinder.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ib.qiblafinder.QiblaDegreeListener
import com.ib.qiblafinder.R
import com.ib.qiblafinder.sensor.QiblaSensor
import com.ib.qiblafinder.sensor.QiblaSensorEventListener
import com.ib.qiblafinder.utils.LocationCoordinates


class QiblaCompassViewV2 : FrameLayout, QiblaSensorEventListener {

    private var currentDegree = 0f
    private  var  qiblaSensor: QiblaSensor?= null
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
            qiblaSensor?.currentLocation = LocationCoordinates(currentLocation.latitude, currentLocation.longitude)
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


        qiblaSensor = QiblaSensor(this.context)

        imageNeedle.rotation = rotationDegree
        imageNeedle.refreshDrawableState()

        qiblaSensor?.register(this)

        if(latitude !=0f && longitude !=0f) {
            currentLocation.apply {
                this.latitude = latitude.toDouble()
                this.longitude = longitude.toDouble()
            }

            qiblaSensor?.currentLocation = LocationCoordinates(latitude.toDouble(), longitude.toDouble())
        }

        invalidateUI()

        if(currentDegree != 0f) {

        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        qiblaSensor?.unregister()
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

    override fun onDeviceAngle(angle: Int) {
        if (angle <= -35 || angle >= 35) {
            line.visibility = View.VISIBLE
        } else {
            line.visibility = View.VISIBLE
        }
    }

    override fun setDirectionRotation(angle: Float) {
        this.imageNeedle.rotation = angle
    }

    override fun setDialRotation(angle: Float) {
        try {
            imageDial.rotation = angle
        } catch ( exception: Exception) {
            degreeListener?.onDegreeChange(currentDegree)
        }
    }
}

