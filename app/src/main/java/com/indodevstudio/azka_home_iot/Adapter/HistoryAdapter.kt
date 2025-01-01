package com.indodevstudio.azka_home_iot.Adapter

import HistoryResponse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.R


class HistoryAdapter(private val list: ArrayList<HistoryResponse>): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    inner class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(historyResponse: HistoryResponse){
            with(itemView){
                val no : String = "${historyResponse.no}"
                val kredit : String = "${historyResponse.kredit}"
                val debit : String = "${historyResponse.debit}"
                val keterangan : String = "${historyResponse.keterangan}"
                val by : String = "${historyResponse.by}"

                val tvNo = findViewById<TextView>(R.id.tvNo)
                val tvKredit = findViewById<TextView>(R.id.tvKredit)
                val tvDebit = findViewById<TextView>(R.id.tvDebit)
                val tvKeterangan = findViewById<TextView>(R.id.tvKeterangan)
                val tvAdded = findViewById<TextView>(R.id.tvAdded)

                tvNo.text = no
                tvKredit.text = kredit
                tvDebit.text = debit
                tvKeterangan.text = keterangan
                tvAdded.text = by
            }

        }
    }

    fun clearData() {
        list.clear() // Clear the list
        notifyDataSetChanged() // Notify the adapter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_table_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(list[position])
    }
}