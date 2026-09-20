package com.keshav.roadsense

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class PotholeListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pothole_list)

        val listView = findViewById<ListView>(R.id.potholeListView)

        val potholeStrings = PotholeRepository.potholeList.mapIndexed { index, p ->

            val time = android.text.format.DateFormat.format("hh:mm a", p.timestamp)

            "Pothole #${index + 1}\n" +
                    "📍 Lat: ${p.latitude}\n" +
                    "📍 Long: ${p.longitude}\n" +
                    "🕒 Time: $time\n"
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            potholeStrings
        )
        listView.adapter = adapter
    }
}