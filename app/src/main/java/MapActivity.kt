
    package com.keshav.roadsense

    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import org.osmdroid.config.Configuration
    import org.osmdroid.views.MapView
    import org.osmdroid.views.overlay.Marker
    import org.osmdroid.util.GeoPoint
    import android.Manifest
    import android.content.pm.PackageManager
    import android.location.Location
    import android.location.LocationListener
    import android.location.LocationManager
    import androidx.core.app.ActivityCompat

    private var userMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    class MapActivity : AppCompatActivity() {

        private lateinit var map: MapView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            Configuration.getInstance().load(
                applicationContext,
                getSharedPreferences("osmdroid", MODE_PRIVATE)
            )

            map = MapView(this)
            setContentView(map)

            map.setMultiTouchControls(true)

            val potholes = PotholeRepository.potholeList

            if (potholes.isNotEmpty()) {

                // Use first pothole as center
                val first = potholes[0]
                val centerPoint = GeoPoint(first.latitude, first.longitude)

                // Set zoom and center ONCE
                map.controller.setZoom(16.0)
                map.controller.setCenter(centerPoint)

                for (p in potholes) {

                    val point = GeoPoint(p.latitude, p.longitude)

                    val marker = Marker(map)
                    marker.position = point
                    marker.title = "Pothole"
                    marker.subDescription = "Lat: ${p.latitude}, Lng: ${p.longitude}"

                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    map.overlays.add(marker)
                }

            } else {

                // Default location (India center)
                val defaultPoint = GeoPoint(20.5937, 78.9629)
                map.controller.setZoom(5.0)
                map.controller.setCenter(defaultPoint)
            }
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {

                // 🔥 1. Get last known location (instant)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (lastLocation != null) {
                    showUserOnMap(lastLocation)
                }

                // 🔥 2. Start live updates
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    1f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            showUserOnMap(location)
                        }
                    }
                )
            }

                }
        private fun showUserOnMap(location: Location) {

            val userPoint = GeoPoint(location.latitude, location.longitude)

            map.controller.setZoom(17.0)
            map.controller.setCenter(userPoint)

            if (userMarker == null) {
                // First time → create marker
                userMarker = Marker(map)
                userMarker!!.position = userPoint
                userMarker!!.title = "You are here"
                map.overlays.add(userMarker)
            } else {
                // Update existing marker
                userMarker!!.position = userPoint
            }

            map.invalidate()
        }

        }

