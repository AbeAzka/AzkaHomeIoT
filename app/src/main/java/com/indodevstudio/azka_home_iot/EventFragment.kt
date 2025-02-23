package com.indodevstudio.azka_home_iot

import Event
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.Adapter.EventAdapter
import com.indodevstudio.azka_home_iot.Model.EventViewModel
import java.util.Calendar


class EventFragment : Fragment() {
    private lateinit var viewModel: EventViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var inputEventName: EditText
    private lateinit var inputEventDate: EditText
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(EventViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerView)
        inputEventName = view.findViewById(R.id.inputEventName)
        inputEventDate = view.findViewById(R.id.inputEventDate)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(listOf()) { event ->
            viewModel.deleteEvent(event.id)
        }
        recyclerView.adapter = adapter

        inputEventDate.isFocusable = false
        inputEventDate.isClickable = true

        inputEventDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$year-${month + 1}-$dayOfMonth"
                    inputEventDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnSubmit.setOnClickListener {
            val name = inputEventName.text.toString()
            val date = inputEventDate.text.toString()
            if (name.isNotEmpty() && date.isNotEmpty()) {
                viewModel.addEvent(Event(0, name, date))
                inputEventName.text.clear()
                inputEventDate.text.clear()
            } else {
                Toast.makeText(requireContext(), "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { events ->
            if (events != null) {
                adapter.updateEvents(events)
            }
        }

        viewModel.fetchEvents()

        return view
    }


}