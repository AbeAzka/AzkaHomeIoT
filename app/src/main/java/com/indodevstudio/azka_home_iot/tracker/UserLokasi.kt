package com.indodevstudio.azka_home_iot.tracker

data class UserLokasi(
    val user_id: String,
    val latitude: Double,
    val longitude: Double,
    val waktu: String
)

data class AllUserResponse(
    val status: String,
    val data: List<UserLokasi>
)