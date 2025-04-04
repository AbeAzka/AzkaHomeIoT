package com.indodevstudio.azka_home_iot.API
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class TandonData(
    val date: String,
    val tandon: Float
)



interface TandonService {
    @GET("data.php") // ganti sesuai path PHP kamu
    fun getTandonData2(): Call<ArrayList<TandonData>>

    @GET("data2.php")
    fun getTandonData(
        @Query("start") startDate: String?,
        @Query("end") endDate: String?
    ): Call<ArrayList<TandonData>>

}
