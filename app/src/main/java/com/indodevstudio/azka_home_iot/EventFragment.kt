package com.indodevstudio.azka_home_iot

import Event
import EventDecorator
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.Adapter.EventAdapter
import com.indodevstudio.azka_home_iot.Model.EventViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import java.util.Locale


class EventFragment : Fragment() {
    private lateinit var viewModel: EventViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var inputEventName: EditText
    private lateinit var inputEventDate: EditText
    private lateinit var btnSubmit: Button
    private lateinit var calendarView: MaterialCalendarView
    //private  lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(EventViewModel::class.java)

        //emptyTextView = view.findViewById(R.id.emptyTextView)
        calendarView = view.findViewById(R.id.calendarView)
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

        val today = CalendarDay.today()
        calendarView.setSelectedDate(today)

        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModel.filterEventsByDate(formattedDate)


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
                Toast.makeText(requireContext(), "Sukses menambahkan agenda", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.filteredEvents.observe(viewLifecycleOwner) { events ->
//            if (events != null) {
            if (events != null) {
                adapter.updateEvents(events)
                markEventsOnCalendar(events)
            }

            //}
        }

        viewModel.fetchEvents()

        calendarView.setOnDateChangedListener { _, date, _ ->
            val calendar = Calendar.getInstance().apply {
                set(date.year, date.month - 1, date.day)
            }
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            viewModel.filterEventsByDate(selectedDate) // Memfilter event berdasarkan tanggal
        }





        return view
    }

    private fun markEventsOnCalendar(events: List<Event>) {
        calendarView.removeDecorators()

        for (event in events) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date)
            date?.let {
                val calendar = Calendar.getInstance().apply { time = date }
                val eventDate = CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1, // Perbaikan: bulan di Java dimulai dari 0, tambah 1
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                calendarView.addDecorator(EventDecorator(Color.RED, eventDate))
            }
        }
    }


}