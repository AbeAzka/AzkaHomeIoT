package com.indodevstudio.azka_home_iot.Model

data class DeviceModel(
    var id: String,
    var name: String,
    val ipAddress: String,
    var isShared: Boolean = false) // 🔹 Tambahan: Menandai apakah perangkat shared)
