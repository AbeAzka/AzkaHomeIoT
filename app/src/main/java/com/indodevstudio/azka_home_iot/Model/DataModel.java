package com.indodevstudio.azka_home_iot.Model;

public class DataModel {
    private int no;
    private String date, message, topic, suhu, kelembapan;


    public int getNo(){
        return no;
    }
    public String getDate(){
        return date;
    }
    public String getMessage(){
        return message;
    }
    public String getTopic(){
        return topic;
    }

    public String getSuhu(){
        return suhu;
    }
    public String getKelembapan(){
        return kelembapan;
    }

}
