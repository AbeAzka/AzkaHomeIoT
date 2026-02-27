package com.indodevstudio.azka_home_iot.API

import com.indodevstudio.azka_home_iot.Model.TimelineEvent
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FireService {
    @GET("timeline.php")
    fun getDeviceTimeline(
        @Query("device_id") deviceId: String
    ): Call<List<TimelineEvent>>
}
