package com.sd.customtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalTime


class MainViewModel : ViewModel() {
    private val dateNow: LocalTime
        get() = LocalTime.now()

    val timeLive: MutableLiveData<LocalTime>
        get() = MutableLiveData(dateNow)

}