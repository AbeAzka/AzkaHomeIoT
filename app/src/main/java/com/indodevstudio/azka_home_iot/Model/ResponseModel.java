package com.indodevstudio.azka_home_iot.Model;
import java.util.List;
public class ResponseModel {
    private int kode;
    private String pesan, kredit, debit, keterangan;
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

    public String getKredit(){return kredit;}
    public String getDebdit(){return debit;}
    public String getKeterangan(){return keterangan;}
}
