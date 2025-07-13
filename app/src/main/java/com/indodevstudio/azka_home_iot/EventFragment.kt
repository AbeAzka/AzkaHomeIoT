package com.indodevstudio.azka_home_iot

import Event
import Event2
import Event3
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.Adapter.EventAdapter
import com.indodevstudio.azka_home_iot.Decorator.EventDecorator

import com.indodevstudio.azka_home_iot.Model.EventViewModel
import com.indodevstudio.azka_home_iot.Decorator.MultiEventDecorator
import com.indodevstudio.azka_home_iot.Decorator.SingleEventDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar

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
    private var selectedColor: Int = Color.BLUE // Default warna
    private lateinit var textSelectedColor: TextView
    var email = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]

        //emptyTextView = view.findViewById(R.id.emptyTextView)
/*        calendarView = view.findViewById(R.id.calendarView)*/
        recyclerView = view.findViewById(R.id.recyclerView)
        inputEventName = view.findViewById(R.id.inputEventName)
        inputEventDate = view.findViewById(R.id.inputEventDate)
/*        textSelectedColor = view.findViewById(R.id.textSelectedColor)*/
        btnSubmit = view.findViewById(R.id.btnSubmit)
        val emptyTextView = view.findViewById<TextView>(R.id.emptyTextView)
        val userData = getUserData()
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val user = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null){
            email = user!!.email.toString()

        }
        if(authToken != null){
            email = userData["email"].toString()
        }

        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.idxxs)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.constrainHeight(R.id.recyclerView, 600) // Atur tinggi sesuai kebutuhan
        constraintSet.applyTo(constraintLayout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(ArrayList()) { events ->
            viewModel.deleteEvent(events.id, email)
        }
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        viewModel.fetchEvents(email)
        viewModel.events.observe(viewLifecycleOwner) { events ->
            if (events != null) {

                    Log.d("EventFragment", "Update RecyclerView dengan ${events.size} event")
            }
            adapter.updateEvents(events ?: arrayListOf()) // Pastikan list tidak null
            adapter.notifyDataSetChanged()
            if (events?.isEmpty() == true) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        // Fetch events dari API

        /*view.findViewById<Button>(R.id.btnPickColor).setOnClickListener {
            val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA)
            val colorNames = arrayOf("Merah", "Biru", "Hijau", "Kuning", "Cyan", "Magenta")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Pilih Warna Event")
            builder.setItems(arrayOf("Merah", "Biru", "Hijau", "Kuning", "Cyan", "Magenta")) { _, which ->
                selectedColor = colors[which]
                // ✅ Update TextView dengan warna yang dipilih
                textSelectedColor.text = "Warna dipilih: ${colorNames[which]}"
                textSelectedColor.setTextColor(selectedColor) // Tampilkan warna secara langsung di TextView44
                Toast.makeText(requireContext(), "Warna dipilih!", Toast.LENGTH_SHORT).show()
            }
            builder.create().show()
        }*/


        /*recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(listOf()) { event ->
            viewModel.deleteEvent(event.id)
        }
        recyclerView.adapter = adapter*/

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
            val userId = getUserIdFromSharedPref() // Ambil user_id dari SharedPreferences

            if (name.isNotEmpty() && date.isNotEmpty() ) {
                if (email != null) {
                    viewModel.addEvent(Event3(0, name, date, email), email)
                }
                inputEventName.text.clear()
                inputEventDate.text.clear()
                /*viewModel.sendNotification(userId, "Event Baru", "Kamu punya event: $name pada $date")*/
                Toast.makeText(requireContext(), "Sukses menambahkan agenda", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
            }
        }


        inputEventDate.isFocusable = false
        inputEventDate.isClickable = true

        /*val today = Calendar.getInstance()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
        calendarView.selectedDate =
            CalendarDay.from(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
        viewModel.fetchEvents() // Ambil semua event dulu*/
        // ✅ Tambahkan pemanggilan ulang setelah setSelectedDate
/*        calendarView.post {
            viewModel.filterEventsByDate(formattedDate)
        }*/








        /*viewModel.filteredEvents.observe(viewLifecycleOwner) { events ->
            adapter.updateEvents(events)
            adapter.notifyDataSetChanged()
            markEventsOnCalendar(events)

            // ✅ Tampilkan "No events" jika daftar event kosong
            if (events.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
            } else {
                emptyTextView.visibility = View.GONE
            }
        }*/


       /* calendarView.setOnDateChangedListener { _, date, _ ->
            val calendar = Calendar.getInstance().apply {
                set(date.year, date.month - 1, date.day)
            }
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            viewModel.selectedDate.value = selectedDate // 🔄 Simpan tanggal yang dipilih
            viewModel.filterEventsByDate(selectedDate) // Memfilter event berdasarkan tanggal
        }


*/



        return view
    }



    private fun getUserData(): Map<String, String?> {
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isVerified" to prefs.getString("isVerified", null)
        )
    }

    private fun markEventsOnCalendar(events: List<Event>) {
        calendarView.removeDecorators() // Hapus dekorasi lama

        val eventMap = mutableMapOf<CalendarDay, MutableList<Int>>()

        for (event in events) {
            val calendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date)!!
            }
            val day = CalendarDay.from(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            if (!eventMap.containsKey(day)) {
                eventMap[day] = mutableListOf()
            }
            eventMap[day]?.add(event.color)
        }

        for ((date, colors) in eventMap) {
            if (colors.size > 1) {
                // Jika ada lebih dari 1 event, tampilkan warna setengah-setengah
                calendarView.addDecorator(EventDecorator(requireContext(), date, colors))
            } else {
                // Jika hanya ada 1 warna, tampilkan warna biasa
                calendarView.addDecorator(SingleEventDecorator(requireContext(), date, colors[0]))
            }
        }
    }

    private fun getUserIdFromSharedPref(): Int {
        val sharedPref = requireContext().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        return sharedPref.getInt("USER_ID", -1)
    }




}