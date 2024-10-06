package com.indodevstudio.azka_home_iot.Adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.indodevstudio.azka_home_iot.API.APIRequestData;
import com.indodevstudio.azka_home_iot.API.RetroServer;
import com.indodevstudio.azka_home_iot.InboxActivity;
import com.indodevstudio.azka_home_iot.Model.DataModel;
import com.indodevstudio.azka_home_iot.Model.ResponseModel;
import com.indodevstudio.azka_home_iot.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class AdapterData extends RecyclerView.Adapter<AdapterData.HolderData> {
    private Context ctx;
    private List<DataModel> listData;
    private List<DataModel> listLaundry;
    private int idPa;

    public AdapterData(Context ctx, List<DataModel> listData){
        this.ctx = ctx;
        this.listData = listData;
    }
    @NonNull
    @Override
    public HolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item,parent,false);
        HolderData holder = new HolderData(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderData holder, int position) {
        DataModel dm = listData.get(position);
        holder.vId.setText(String.valueOf(dm.getNo()));
        holder.vMessage.setText(dm.getMessage());
        holder.vTopic.setText(dm.getTopic());
        holder.vDate.setText(dm.getDate());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class HolderData extends RecyclerView.ViewHolder{
        TextView vId, vMessage, vTopic, vDate;


        public HolderData(@NonNull View itemView) {
            super(itemView);

            vId = itemView.findViewById(R.id.v_id);
            vMessage = itemView.findViewById(R.id.v_message);
            vDate = itemView.findViewById(R.id.v_date);
            vTopic = itemView.findViewById(R.id.v_topic);


        }

        private void getData(){
            APIRequestData ardData = RetroServer.konekRetrofit().create(APIRequestData.class);
            Call<ResponseModel> ambilData = ardData.ardGetData(idPa);

            ambilData.enqueue(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    int kode = response.body().getKode();
                    String pesam = response.body().getPesan();
                    listLaundry = response.body().getData();

                    int varIdPa = listLaundry.get(0).getNo();
                    String varMessage = listLaundry.get(0).getMessage();
                    String varTopic = listLaundry.get(0).getTopic();
                    String varDate = listLaundry.get(0).getDate();


                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    Toast.makeText(ctx, "Failed connecting to the server : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("ERROR", "Failed connecting to the server : " + t.getMessage());
                }
            });
        }
    }
}
