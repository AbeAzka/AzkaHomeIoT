package com.indodevstudio.azka_home_iot.API

import okhttp3.OkHttpClient

object ApiClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }
}