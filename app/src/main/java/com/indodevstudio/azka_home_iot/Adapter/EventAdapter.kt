package com.indodevstudio.azka_home_iot.Adapter

import Event
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.R

class EventAdapter(private var events: List<Event>, private val onDelete: (Event) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.eventName)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
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

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = if (newEvents.isEmpty()) {
            listOf(Event(0, "Tidak ada event", "", Color.BLUE))
        } else {
            newEvents
        }
        notifyDataSetChanged()
    }
}
