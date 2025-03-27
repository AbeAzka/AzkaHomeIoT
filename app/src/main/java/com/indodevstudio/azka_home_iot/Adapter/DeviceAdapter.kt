package com.indodevstudio.azka_home_iot.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.DeviceControlActivity
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.R

class DeviceAdapter(
    private val deviceList: MutableList<DeviceModel>,
    private val listener: DeviceActionListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    interface DeviceActionListener {
        fun onRenameDevice(device: DeviceModel, position: Int)
        fun onDeleteDevice(device: DeviceModel, position: Int)
        fun onResetWiFi(device: DeviceModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device, listener, position)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DeviceControlActivity::class.java)
            intent.putExtra("deviceName", device.name)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = deviceList.size

    fun updateData(newDevices: List<DeviceModel>) {
        deviceList.clear()
        deviceList.addAll(newDevices)
        notifyDataSetChanged()
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val btnRename: ImageButton = itemView.findViewById(R.id.btnRename)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnResetWiFi: ImageButton = itemView.findViewById(R.id.btnResetWiFi)

        fun bind(device: DeviceModel, listener: DeviceActionListener, position: Int) {
            tvDeviceName.text = device.name

            btnRename.setOnClickListener { listener.onRenameDevice(device, position) }
            btnDelete.setOnClickListener { listener.onDeleteDevice(device, position) }
            btnResetWiFi.setOnClickListener { listener.onResetWiFi(device) }
        }
    }
}
