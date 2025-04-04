package com.indodevstudio.azka_home_iot.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TandonClient {
    private const val BASE_URL = "https://abeazka.my.id/telemetri/" // ganti sesuai alamat kamu

    val instance: TandonService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TandonService::class.java)
    }
}
