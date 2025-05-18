package com.indodevstudio.azka_home_iot.API


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
data class DeviceStatusResponse(

    val status_s1: String?, // misal "online", "offline"
    val status_s2: String?, // misal "online", "offline"
    val success: Boolean,
)

data class StatusResponse(
    val status: String
)

interface ArduinoService {
    @GET("get_status.php")
    fun getDeviceStatus(
        @Query("device_id") deviceId: String
    ): Call<DeviceStatusResponse>

    @GET("status_devices.php") // sesuaikan dengan nama file PHP kamu
    fun getStatus(@Query("device_id") deviceId: String): Call<StatusResponse>
}