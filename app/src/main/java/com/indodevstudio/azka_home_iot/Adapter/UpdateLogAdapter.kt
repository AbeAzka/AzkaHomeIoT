package com.indodevstudio.azka_home_iot.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.indodevstudio.azka_home_iot.API.UpdateLogf
import com.indodevstudio.azka_home_iot.R

class UpdateLogAdapter(private val logs: ArrayList<UpdateLogf>) : RecyclerView.Adapter<UpdateLogAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.titleText)
        val content = itemView.findViewById<TextView>(R.id.contentText)
        val date = itemView.findViewById<TextView>(R.id.dateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_update_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.title.text = "v${log.versi} - ${log.title}"
        holder.content.text = log.deskripsi
        holder.date.text = log.tanggal
    }

    override fun getItemCount(): Int = logs.size
}
