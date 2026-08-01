package com.indodevstudio.azka_home_iot.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.Model.TimelineEvent
import com.indodevstudio.azka_home_iot.R
import java.text.SimpleDateFormat
import java.util.Locale

class TimelineAdapter(private val items: List<TimelineEvent>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        val tvEventDesc: TextView = itemView.findViewById(R.id.tvEventDesc)
        val viewCircle: View = itemView.findViewById(R.id.viewCircle)
        val viewLine: View = itemView.findViewById(R.id.viewLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_event, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = items[position]

        holder.tvEventTitle.text = item.status
        val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val date = inputFormat.parse(item.time)
        holder.tvEventTime.text = date?.let { outputFormat.format(it) } ?: "-"

        holder.tvEventDesc.text = item.description

        // Hilangkan garis di item terakhir
        holder.viewLine.visibility = if (position == 0) View.GONE else View.VISIBLE

        // Ganti warna bulatan ON/OFF
        val colorRes = if (item.status == "ON") android.R.color.holo_green_light else android.R.color.holo_red_light
        holder.viewCircle.setBackgroundColor(holder.itemView.context.getColor(colorRes))
    }

    override fun getItemCount(): Int = items.size
}
