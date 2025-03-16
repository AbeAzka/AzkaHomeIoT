package com.indodevstudio.azka_home_iot.Adapter

import Event
import Event2
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.R


class EventAdapter(private var eventList: ArrayList<Event2>, private val onDelete: (Event2) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date.ifEmpty { "-" }

        // Jika event tidak valid (misal, hanya placeholder "Tidak ada event"), sembunyikan tombol hapus
        if (event.id == 0) {
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDelete(event) }
        }
    }

    fun updateEvents(newEvents: List<Event2>) {
        eventList.clear()
        eventList.addAll(newEvents)
        notifyDataSetChanged() // Pastikan RecyclerView diperbarui
    }


    override fun getItemCount(): Int = eventList.size
}

