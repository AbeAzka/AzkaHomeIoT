package com.indodevstudio.azka_home_iot

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.indodevstudio.azka_home_iot.API.APIRequestData
import com.indodevstudio.azka_home_iot.API.RetroServer
import com.indodevstudio.azka_home_iot.Adapter.AdapterData
import com.indodevstudio.azka_home_iot.Model.DataModel
import com.indodevstudio.azka_home_iot.Model.ResponseModel
import com.indodevstudio.azka_home_iot.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InboxActivity : AppCompatActivity() {

    lateinit var rvData : RecyclerView
    var test : RecyclerView? = null
    var adData : RecyclerView.Adapter<*>? = null
    var lmData : RecyclerView.LayoutManager? = null
    var listData: List<DataModel> = ArrayList<DataModel>()
    var srlData: SwipeRefreshLayout? = null
    var pbData: ProgressBar? = null
    private lateinit var binding : ActivityMainBinding
    lateinit var shimmerFrame : ShimmerFrameLayout
    lateinit var text : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val spinner: Spinner = findViewById(R.id.spinner_topic)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Inbox"
        val tvFilterStatus = findViewById<TextView>(R.id.tv_filter_status)

        // List topik yang tersedia
        val topicList = listOf("Semua", "Arduino Control", "Tandon", "Movement Detection")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, topicList)
        spinner.adapter = adapter


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTopic = parent.getItemAtPosition(position).toString()
                rvData.removeAllViewsInLayout()
                shimmerFrame.startShimmer()
                shimmerFrame.visibility = View.VISIBLE
                if (selectedTopic == "Semua") {
                    tvFilterStatus.text = "Showing All Data"
                } else {
                    tvFilterStatus.text = "Showing Data with Topic: \"$selectedTopic\""
                }
                retrieveData(selectedTopic)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        text = findViewById(R.id.title_data_inbox)
        shimmerFrame = findViewById(R.id.shimmerLayout);
        shimmerFrame.startShimmer();
        shimmerFrame.setVisibility(View.VISIBLE);
        rvData = findViewById(R.id.rv_data)
        srlData = findViewById(R.id.srl_data)
        pbData = findViewById(R.id.pb_data)



        lmData = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (lmData as LinearLayoutManager).setReverseLayout(true);
        (lmData as LinearLayoutManager).setStackFromEnd(true);
        with(rvData){this?.setLayoutManager(lmData)}

        with (srlData){
            this?.setOnRefreshListener {
                setRefreshing(true)
                shimmerFrame.startShimmer();
                shimmerFrame.setVisibility(View.VISIBLE);
                rvData.removeAllViewsInLayout()
                retrieveData("Semua")
                setRefreshing(false)

            }
        }

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
//        if (savedInstanceState == null) {
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.inbox, InboxActivity.InboxFragment())
//                .commit()
//        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class InboxFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
    override fun onResume(){
        super.onResume()
        retrieveData("Semua")
    }
    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }

    fun retrieveData(topic: String){
        pbData!!.visibility = View.VISIBLE

        val ardData: APIRequestData = RetroServer.konekRetrofit().create(APIRequestData::class.java)
//        val tampilData: Call<ResponseModel> = ardData.ardRetrieveData2()
        val tampilData: Call<ResponseModel> = if (topic == "Semua") {
            ardData.ardRetrieveData2("") // ambil semua data
        } else {
            ardData.ardRetrieveData2(topic)
        }
        tampilData.enqueue(object: Callback<ResponseModel>{
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {

                if(response.body()?.data == null){
                    rvData?.visibility = View.GONE
                    text.visibility = View.VISIBLE
                    pbData?.visibility = View.INVISIBLE
                }else{
                    shimmerFrame.stopShimmer();
                    shimmerFrame.setVisibility(View.GONE);
                    listData = response.body()!!.data
                    adData = AdapterData(this@InboxActivity, listData)
                    rvData?.smoothScrollToPosition(listData.size-1);
                    rvData?.visibility = View.VISIBLE

                    text.visibility = View.GONE
                    rvData?.adapter = adData

                    adData?.notifyDataSetChanged()
                    pbData?.visibility = View.INVISIBLE
                }


            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(
                    this@InboxActivity,"Failed to connect: " + t.message, Toast.LENGTH_SHORT
                ).show()
                Log.i("ERROR", "Failed to connect: " + t.message)
                pbData?.visibility = View.INVISIBLE
            }

        })
    }
}