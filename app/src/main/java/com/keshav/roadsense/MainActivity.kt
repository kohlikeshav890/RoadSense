package com.keshav.roadsense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
private lateinit var potholeDetectedText: TextView
private lateinit var totalPotholesText: TextView
private var lastPotholeCount = 0
class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var viewButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        viewButton = findViewById(R.id.viewPotholesButton)
        statusText = findViewById(R.id.statusText)
        potholeDetectedText = findViewById(R.id.potholeDetectedText)
        totalPotholesText = findViewById(R.id.totalPotholesText)
        startButton.setOnClickListener {
            val intent = Intent(this, DetectionService::class.java)
            startForegroundService(intent)
            statusText.text = "Status: Detecting..."
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, DetectionService::class.java)
            stopService(intent)
            statusText.text = "Status: Stopped"
        }

        viewButton.setOnClickListener {
            val intent = Intent(this, PotholeListActivity::class.java)
            startActivity(intent)
        }


        val handler = android.os.Handler(mainLooper)

        val runnable = object : Runnable {
            override fun run() {

                val currentCount = PotholeRepository.potholeList.size

                totalPotholesText.text = "Total Potholes: $currentCount"

                // 🔥 Detect NEW pothole
                if (currentCount > lastPotholeCount) {

                    potholeDetectedText.text = "⚠ Pothole Detected!"

                    // Hide after 2 seconds
                    handler.postDelayed({
                        potholeDetectedText.text = "Detecting Potholes..."
                    }, 2000)
                }

                lastPotholeCount = currentCount

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)

        handler.post(runnable)
        val mapButton = findViewById<Button>(R.id.viewMapButton)

        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
}
}