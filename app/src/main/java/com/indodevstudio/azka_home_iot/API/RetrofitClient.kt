package com.indodevstudio.azka_home_iot.API

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit


object RetrofitClient {
    private const val BASE_URL = "https://ahi.abeazka.my.id/api/fcm/"  // Ganti dengan URL backend kamu


    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}
