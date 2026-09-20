package com.keshav.roadsense

import Pothole
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
private var currentLatitude = 0.0
private var currentLongitude = 0.0
private var isLocationReady = false

class DetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var locationManager: LocationManager

    private var previousZ = 0f
    private var isFirstReading = true
    private var currentSpeed = 0f

    private var lastDetectionTime: Long = 0
    private val cooldownMillis = 3000

    override fun onCreate() {
        super.onCreate()

        startForegroundServiceNotification()

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        Log.d("RoadSense", "Accelerometer started")

        // Initialize GPS for speed
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0f,
                object : LocationListener {

                    override fun onLocationChanged(location: Location) {

                        currentSpeed = location.speed * 3.6f

                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        isLocationReady = true

                        Log.d("RoadSense", "Speed: $currentSpeed km/h")
                        Log.d("RoadSense", "Location: $currentLatitude , $currentLongitude")
                    }
                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {}

                }
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val z = event.values[2]
            if (isFirstReading) {
                previousZ = z
                isFirstReading = false
                return
            }

            val jerk = Math.abs(z - previousZ)

            previousZ = z

            Log.d("RoadSense", "Jerk: $jerk")

            val threshold = 9

            val currentTime = System.currentTimeMillis()

            if (jerk > threshold &&
                currentTime - lastDetectionTime > cooldownMillis
            ){

                lastDetectionTime = currentTime

                Log.d("RoadSense", "⚠ POTHOLE DETECTED")
                val pothole = Pothole(
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    timestamp = System.currentTimeMillis()
                )

                PotholeRepository.addPothole(pothole)

                Log.d("RoadSense", "Total Potholes: ${PotholeRepository.getCount()}")
                if (isLocationReady) {
                    Log.d("RoadSense", "Pothole Location: $currentLatitude , $currentLongitude")
                } else {
                    Log.d("RoadSense", "Location not ready yet")
                }
                val intent = Intent("POTHOLE_EVENT")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(this)

        try {
            locationManager.removeUpdates { }
        } catch (e: Exception) {
        }

        Log.d("RoadSense", "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundServiceNotification() {

        val channelId = "roadsense_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "RoadSense Detection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RoadSense Running")
            .setContentText("Detecting road anomalies...")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .build()

        startForeground(1, notification)
    }
}