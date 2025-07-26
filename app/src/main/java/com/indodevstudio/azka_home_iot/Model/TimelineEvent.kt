package com.indodevstudio.azka_home_iot.Model

data class TimelineEvent(
    val time: String,    // misal "2025-07-26 21:30"
    val status: String,  // "ON" atau "OFF"
    val description: String
)
