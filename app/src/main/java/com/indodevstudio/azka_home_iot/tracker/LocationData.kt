package com.indodevstudio.azka_home_iot.tracker

import androidx.lifecycle.MutableLiveData

data class Koordinat(
    val user_id: String,
    val latitude: Double,
    val longitude: Double
)

object LocationData {
    val locationLiveData = MutableLiveData<Koordinat>()
}