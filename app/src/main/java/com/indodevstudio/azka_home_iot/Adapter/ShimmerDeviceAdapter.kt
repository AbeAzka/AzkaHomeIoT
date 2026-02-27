package com.indodevstudio.azka_home_iot.Adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.R
import android.view.LayoutInflater
import android.view.View

class ShimmerDeviceAdapter(private val itemCount: Int) :
    RecyclerView.Adapter<ShimmerDeviceAdapter.ShimmerViewHolder>() {

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // Tidak ada data yang di-bind, karena ini placeholder
    }

    override fun getItemCount(): Int = itemCount
}
