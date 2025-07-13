package com.indodevstudio.azka_home_iot.API

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface EventService {
    @FormUrlEncoded
    @POST("save_fcm_token.php")
    fun sendFcmToken(
        @Field("email") email: String,
        @Field("fcm_token") fcmToken: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("send_notification.php")
    fun sendNotification(
        @Field("user_id") userId: Int,
        @Field("title") title: String,
        @Field("body") body: String
    ): Call<ResponseBody>

    @POST("api/events/mark_complete.php")
    @FormUrlEncoded
    fun markEventsAsDone(
        @Field("date") date: String,
        @Field("events[]") events: List<String>
    ): Call<ResponseBody>
}