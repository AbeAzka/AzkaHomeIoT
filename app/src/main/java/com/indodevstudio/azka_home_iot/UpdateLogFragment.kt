package com.indodevstudio.azka_home_iot

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.API.UpdateLogService
import com.indodevstudio.azka_home_iot.API.UpdateLogf
import com.indodevstudio.azka_home_iot.Adapter.UpdateLogAdapter
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class UpdateLogFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UpdateLogAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchUpdateLogs()
        /*(activity as? AppCompatActivity)?.supportActionBar?.hide()

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarSetup)
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })*/
    }

    fun onBackPressed() {

        // Menggunakan parentFragmentManager untuk mengganti fragmen di dalam aktivitas
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment()) // Gantilah dengan fragmen yang sesuai
            .commit()
    }

    /*override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }*/

    private fun fetchUpdateLogs() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://abeazka.my.id/ahi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(UpdateLogService::class.java)

        val call = apiService.getUpdateLogs()
        call.enqueue(object : retrofit2.Callback<ArrayList<UpdateLogf>> {
            override fun onResponse(
                call: Call<ArrayList<UpdateLogf>>,
                response: retrofit2.Response<ArrayList<UpdateLogf>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!
                    adapter = UpdateLogAdapter(logs)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Response kosong", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.GONE
            }

            override fun onFailure(call: Call<ArrayList<UpdateLogf>>, t: Throwable) {
                Toast.makeText(requireContext(), "Gagal ambil data: ${t.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })
    }


}