package com.indodevstudio.azka_home_iot.API

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit


object RetrofitClient {
    private const val BASE_URL = "https://www.indodevstudio.my.id/" // 👈 base URL kamu

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}
