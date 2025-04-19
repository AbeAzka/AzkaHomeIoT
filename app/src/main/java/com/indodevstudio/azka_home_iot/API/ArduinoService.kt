package com.indodevstudio.azka_home_iot.API


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
data class DeviceStatusResponse(

    val status_s1: String?, // misal "online", "offline"
    val status_s2: String?, // misal "online", "offline"
    val success: Boolean,
)

interface ArduinoService {
    @GET("get_status.php")
    fun getDeviceStatus(
        @Query("device_id") deviceId: String
    ): Call<DeviceStatusResponse>
}