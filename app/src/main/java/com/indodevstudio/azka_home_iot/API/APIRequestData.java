package com.indodevstudio.azka_home_iot.API;
import com.indodevstudio.azka_home_iot.Model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIRequestData {
    @GET("retrieve.php")
    Call<ResponseModel> ardRetrieveData();

    @GET("retrieve2.php")
    Call<ResponseModel> ardRetrieveData2(
            @Query("topic") String topic
    );

    @GET("retrieve_dht.php")
    Call<ResponseModel> ardRetrieveTemp();

    @FormUrlEncoded
    @POST("get.php")
    Call<ResponseModel> ardGetData(
            @Field("no") int no
    );
}
