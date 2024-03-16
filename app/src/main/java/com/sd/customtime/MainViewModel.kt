package com.sd.customtime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MainViewModel:ViewModel() {
    private val _timeLiveData: MutableLiveData<List<Double>> = MutableLiveData()
    val timeLiveData: LiveData<List<Double>>
        get() = _timeLiveData

    init {

    }
}