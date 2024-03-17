package com.sd.customtime.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.sd.customtime.MainViewModel
import com.sd.customtime.R
import com.sd.customtime.custom.TimePaintView
import com.sd.customtime.custom.TimeView

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel: MainViewModel by viewModels()
    private var time: TimeView? = null
    private var timePaint: TimePaintView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        time = findViewById(R.id.time)
        timePaint = findViewById(R.id.time2)
    }

    override fun onResume() {
        super.onResume()

        viewModel.timeLive.observe(this) {
            val list = listOf(
                it.hour.toDouble(),
                it.minute.toDouble(),
                it.second.toDouble()
            )
            time?.data = list
            timePaint?.data = list

            Toast.makeText(this@MainActivity, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}