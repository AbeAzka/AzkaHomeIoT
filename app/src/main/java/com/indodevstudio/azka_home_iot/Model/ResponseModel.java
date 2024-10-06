package com.indodevstudio.azka_home_iot.Model;
import java.util.List;
public class ResponseModel {
    private int kode;
    private String pesan;
    private List<DataModel>data;

    public int getKode(){
        return kode;
    }
    public String getPesan(){
        return pesan;
    }

    public List<DataModel> getData(){
        return data;
    }
}
