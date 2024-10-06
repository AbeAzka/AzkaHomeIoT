package com.indodevstudio.azka_home_iot.API;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroServer {
    private static final String baseURL = "https://abeazka.my.id/arduino_keypad/";
    private static Retrofit retro;

    public static Retrofit konekRetrofit(){
        if(retro == null){
            retro = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retro;
    }
}
