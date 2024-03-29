package com.ib.qiblafinder.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ib.qiblafinder.view.QiblaCompassViewV2
import com.ib.qiblafinder.QiblaDegreeListener
import com.ib.qiblafinder.view.QiblaCompassView

class MainActivity : AppCompatActivity() {
    private lateinit var qiblaCompassView: QiblaCompassViewV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qiblaCompassView = findViewById(R.id.qiblaCompassView)

        //  qiblaCompassView.degree = 138.0f  // Angle from your location to Qibla
        //  qiblaCompassView.location =       // Get your latest location and provide to QiblaCompassView for accurate direction

        qiblaCompassView.degreeListener = object : QiblaDegreeListener {
            override fun onDegreeChange(degree: Float) {
                 // With location change degree will also be changed.
            }
        }
    }
}