package com.indodevstudio.azka_home_iot.API

import retrofit2.Call
import retrofit2.http.GET

data class UpdateLogf(
    val id: Int,
    val versi: String,
    val title: String,
    val deskripsi: String,
    val tanggal: String
)

interface UpdateLogService {
    @GET("get_update_logs.php")
    fun getUpdateLogs(): Call<ArrayList<UpdateLogf>>
}