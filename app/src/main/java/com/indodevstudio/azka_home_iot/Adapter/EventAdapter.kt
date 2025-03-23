package com.indodevstudio.azka_home_iot.Adapter

import ApiService2
import Event
import Event2
import Server2
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response





class EventAdapter(private var eventList: ArrayList<Event2>, private val onDelete: (Event2) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnCheck: ImageButton = itemView.findViewById(R.id.btnCheck)
        val card: CardView = itemView.findViewById(R.id.cardItemEv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date.ifEmpty { "-" }

        // Jika event kosong / placeholder
        if (event.id == 0) {
            holder.btnDelete.visibility = View.GONE
            holder.btnCheck.visibility = View.GONE
        } else {
            holder.btnCheck.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDelete(event) }
        }

        val context = holder.itemView.context

        // **🔹 Perbaikan: Cek `isCompleted` dengan benar**
        val isCompleted = event.isCompleted == 1

        // **🔹 Perbaikan: Ubah warna CardView berdasarkan status**
        val backgroundColor = if (isCompleted) {
            ContextCompat.getColor(context, R.color.green)  // Warna hijau jika selesai
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.drawerItemBackground, typedValue, true)
            typedValue.data
        }
        holder.card.setCardBackgroundColor(backgroundColor)

        // **🔹 Perbaikan: Ubah ikon dan tooltip tombol check**
        val iconRes = if (isCompleted) R.drawable.ic_cancel else R.drawable.ic_check_circle
        val tooltipText = if (isCompleted) "Batalkan selesai" else "Tandai sebagai selesai"

        holder.btnCheck.setImageResource(iconRes)
        holder.btnCheck.tooltipText = tooltipText

        // **🔹 Perbaikan: Ketika tombol check diklik**
        holder.btnCheck.setOnClickListener {
            val newStatus = if (isCompleted) 0 else 1
            updateEventStatus(event.id, newStatus, position, holder)
        }
    }

    private fun updateEventStatus(eventId: Int, isCompleted: Int, position: Int, holder: EventViewHolder) {
        val call = Server2.instance.updateEventStatus(eventId, isCompleted)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    eventList[position].isCompleted = isCompleted
                    notifyItemChanged(position)
                    Toast.makeText(holder.itemView.context, "Status event diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(holder.itemView.context, "Gagal mengupdate status!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateEvents(newEvents: List<Event2>) {
        eventList.clear()
        eventList.addAll(newEvents)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = eventList.size
}




