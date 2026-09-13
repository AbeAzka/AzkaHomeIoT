package com.indodevstudio.azka_home_iot.tracker

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TrackerApi {
    @POST("simpan_lokasi.php")
    suspend fun kirimLokasi(
        @Body data: Koordinat
    ): Response<Unit> // Menggunakan Response<Unit> karena PHP hanya bertugas menyimpan data
}

object TrackerApiClient {
    // URL server PHP kamu
    private const val BASE_URL = "https://www.indodevstudio.my.id/api/v2/ahi/tracker/"

    val instance: TrackerApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(TrackerApi::class.java)
    }
}