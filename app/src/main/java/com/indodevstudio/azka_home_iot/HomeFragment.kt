package com.indodevstudio.azka_home_iot

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.APIRequestData
import com.indodevstudio.azka_home_iot.API.RetroServer
import com.indodevstudio.azka_home_iot.API.TandonClient
import com.indodevstudio.azka_home_iot.API.TandonData
import com.indodevstudio.azka_home_iot.Adapter.AdapterData
import com.indodevstudio.azka_home_iot.Model.DataModel
import com.indodevstudio.azka_home_iot.Model.ResponseModel
import com.ortiz.touchview.TouchImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var listLaundry: List<DataModel>


    lateinit var imageGrafik : TouchImageView
    lateinit var inputStream : InputStream
    var click = false
    var image : Bitmap? = null
    var image2 : Bitmap? = null
    lateinit var text : TextView
    var rvData : RecyclerView? = null
    var test : RecyclerView? = null
    var adData : RecyclerView.Adapter<*>? = null
    var lmData : RecyclerView.LayoutManager? = null
    var listData: List<DataModel> = ArrayList<DataModel>()
    var srlDat: NestedScrollView? = null
    var srlData: SwipeRefreshLayout? = null
    var pbData: ProgressBar? = null
    var cards2: CardView? = null
    lateinit var shimmerLayout : ShimmerFrameLayout
    private lateinit var lineChart: LineChart

    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    lateinit var textTime : TextView
    lateinit var textHum : TextView
    lateinit var textTemo : TextView

    private val refreshHandler = Handler(Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable


    lateinit var text_card : TextView
//    var pbData_BG: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)
        rvData = view.findViewById(R.id.rv_data)
        srlData = view.findViewById(R.id.srl_data)
        pbData = view.findViewById(R.id.pb_data)
        text = view.findViewById(R.id.text_Inbox)
        //srlDat = view.findViewById(R.id.srl_dta)
        lineChart = view.findViewById(R.id.lineChart)
        startDateInput = view.findViewById(R.id.startDateInput)
        endDateInput = view.findViewById(R.id.endDateInput)
//        cards2 = view.findViewById(R.id.cards_info2)
//        text_card = view.findViewById(R.id.pp)
        textTime = view.findViewById(R.id.last_update)
        textHum = view.findViewById(R.id.humidity_txt)
        textTemo = view.findViewById(R.id.temperature_txt)

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sharedPreferences_tutorial = requireContext().getSharedPreferences("AppPrefs",
            Context.MODE_PRIVATE
        )
        val isFirstTime = sharedPreferences_tutorial.getBoolean("isFirstTimes3", true)

        if (isFirstTime) {
            TapTargetSequence(requireActivity())
                .targets(
                    TapTarget.forView(view.findViewById(R.id.humidity_txt), "Humidity", "Informasi tingkat kelembapan.")
                        .cancelable(false)
                        .transparentTarget(true),
                    TapTarget.forView(view.findViewById(R.id.temperature_txt), "Temperature", "Informasi suhu.")
                        .cancelable(false)
                        .transparentTarget(true),
                    TapTarget.forView(view.findViewById(R.id.rv_data), "Latest Inbox", "Menampilkan pesan Inbox terbaru.")
                        .cancelable(false)
                        .transparentTarget(true),
                    TapTarget.forView(view.findViewById(R.id.imageGrafikSampleTest), "Graph", "Menampilkan grafik.")
                        .cancelable(true)
                        .transparentTarget(true),
                    )
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        // Tandai bahwa user sudah pernah lihat tutorial
                        sharedPreferences_tutorial.edit().putBoolean("isFirstTimes3", false).apply()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                }).start()
        }


        startDateInput.setOnClickListener {
            showDatePicker { date ->
                startDateInput.setText(date)
                getChartData() // refresh grafik dengan filter
            }
        }

        endDateInput.setOnClickListener {
            showDatePicker { date ->
                endDateInput.setText(date)
                getChartData()
            }
        }
        getChartData2()
// Set interval auto-refresh
        /*refreshRunnable = object : Runnable {
            override fun run() {
                getChartData() // Panggil fungsi ambil data grafik
                refreshHandler.postDelayed(this, 30000) // 30 detik
            }
        }
        refreshHandler.post(refreshRunnable)*/


//        chart = view.findViewById(R.id.chart)
//        pbData_BG = view.findViewById(R.id.load)
         val imageGraphSample = view.findViewById<ImageView>(R.id.imageGrafikSample)
        val imageGraphSample2 = view.findViewById<ImageView>(R.id.imageGrafikSampleTest)
        lmData = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        (lmData as LinearLayoutManager).setReverseLayout(true);
        (lmData as LinearLayoutManager).setStackFromEnd(true);
        with(rvData){this?.setLayoutManager(lmData)}

        with (srlData) {
            this?.setOnRefreshListener {
                setRefreshing(true)

                // Start the shimmer effect

                shimmerLayout.startShimmer() // Start shimmer
                shimmerLayout.visibility = View.VISIBLE
                // Clear the image initially

                imageGraphSample.setImageBitmap(null)
                lineChart.visibility = View.GONE
                imageGraphSample.visibility = View.GONE
                imageGraphSample2.setImageResource(R.drawable.sample)

                // Simulate your data retrieval methods (replace with your actual logic)
                retrieveData()
                retrieveTemp()
                //retrieveImage()

                // Panggil grafik pakai tanggal default
                getChartData2()



                    // Set the actual image after data has been retrieved
                    // For example, setImageBitmap(actualImage)
                    // imageGrafikSample.setImageBitmap(actualImage)



                setRefreshing(false)
                // Stop the shimmer after a short delay (ensure image retrieval completes first)

            }
        }

        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val greeting = view.findViewById<TextView>(R.id.greetings)

        val displayName = getArguments()?.getString("name")
        val userData = getUserData()
        var namee = ""
//        val imageGraphSample2 = view.findViewById<ImageView>(R.id.imageGrafikSampleHehe)

        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            namee = user?.displayName.toString()
        }
        if(authToken != null){
            namee = userData["username"].toString()
        }





        //END OF MAKE IMG

        imageGraphSample.setOnClickListener{ view->
            if (click == true){
                getActivity()?.runOnUiThread {
                            Toast.makeText(
                                getActivity(),
                                "You already click this image!",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
            }else {
                click = true
                var inflater = LayoutInflater.from(getActivity())
                var popupview = inflater.inflate(R.layout.popup_grafik, null, false)
                var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
                var close = popupview.findViewById<ImageView>(R.id.close)
                var builder = PopupWindow(
                    popupview,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    true
                )
                imagee.setImageBitmap(image)

                imageGraphSample.setImageBitmap(image)

                builder.setBackgroundDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.background
                    )
                )
                builder.animationStyle = R.style.DialogAnimation
                builder.showAtLocation(
                    getActivity()?.findViewById(R.id.drawer_layout),
                    Gravity.CENTER,
                    0,
                    0
                )
                close.setOnClickListener {
                    builder.dismiss()
                    click = false
                }
            }
        }




        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val greetingss: String = if (currentHour in 0..11) {
            "Good morning"
        } else {
            "Good evening"
        }

        greeting.text = "$greetingss, $namee"

//        val current = LocalDateTime.now()
//
//        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
//        val formatted = current.format(formatter)
        //val email2 = user!!.email


//        println("Current Date is: $formatted")


        return view
    }
    override fun onPause() {
        super.onPause()
        //refreshHandler.removeCallbacks(refreshRunnable)
    }
    override fun onResume(){
        super.onResume()
        retrieveData()
        retrieveTemp()
        getChartData2()
        //refreshHandler.post(refreshRunnable)
        /*//retrieveImage()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Tanggal hari ini
        val today = Calendar.getInstance()

        // Tanggal kemarin
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }

        // Set default di input
        startDateInput.setText(dateFormat.format(yesterday.time))
        endDateInput.setText(dateFormat.format(today.time))

        // Panggil grafik pakai tanggal default
        getChartData()*/


    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        context?.let {
            val dpd = DatePickerDialog(it, { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

    }

    private fun getChartData() {
        val start = startDateInput.text.toString().takeIf { it.isNotEmpty() }
        val end = endDateInput.text.toString().takeIf { it.isNotEmpty() }

        TandonClient.instance.getTandonData(start, end).enqueue(object : retrofit2.Callback<ArrayList<TandonData>> {
            override fun onResponse(call: retrofit2.Call<ArrayList<TandonData>>, response: retrofit2.Response<ArrayList<TandonData>>) {
                if (response.isSuccessful) {
                    val chartData = response.body()
                    if (chartData != null) {
                        showLineChart(chartData)
                    }
                    val handler = Handler(Looper.getMainLooper())
                    handler.post{
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE // Hide shimmer after loading
                        lineChart.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<ArrayList<TandonData>>, t: Throwable) {
                context?.let {
                    Toast.makeText(it, "Gagal: ${t.message}", Toast.LENGTH_LONG).show()

                }
            }
        })
    }

    private fun getChartData2() {
        val start = startDateInput.text.toString().takeIf { it.isNotEmpty() }
        val end = endDateInput.text.toString().takeIf { it.isNotEmpty() }

        TandonClient.instance.getTandonData2().enqueue(object : retrofit2.Callback<ArrayList<TandonData>> {
            override fun onResponse(call: retrofit2.Call<ArrayList<TandonData>>, response: retrofit2.Response<ArrayList<TandonData>>) {
                if (response.isSuccessful) {
                    val chartData = response.body()

                    if (chartData != null) {
                        showLineChart(chartData)
                    }
                    val handler = Handler(Looper.getMainLooper())
                    handler.post{
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE // Hide shimmer after loading
                        lineChart.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<ArrayList<TandonData>>, t: Throwable) {
                context?.let {
                    Toast.makeText(it, "Gagal: ${t.message}", Toast.LENGTH_LONG).show()

                }
            }
        })
    }

    private fun showLineChart(data: ArrayList<TandonData>) {
        var textColor: Int? = null
        context?.theme?.let { theme ->
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.textContent, typedValue, true)
            textColor = typedValue.data
            // pakai textColor selanjutnya...
        }


        if (data.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Tidak ada data tandon tersedia")
            lineChart.setNoDataTextColor(Color.RED)
            lineChart.invalidate()
            lineChart.setNoDataText("Tidak ada data tersedia")
            textColor?.let { lineChart.setNoDataTextColor(it) }
            lineChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD)

            return
        }

        val entries = data.mapIndexed { index, item ->
            Entry(index.toFloat(), item.tandon)
        }
        val labelTanggal = data.map { it.date } // ["2025-04-01", "2025-04-02", "2025-04-03"]


        val dataSet = LineDataSet(entries, "Tinggi Air (cm)").apply {
            context?.let {
                color = ContextCompat.getColor(it, R.color.very_blue)

                valueTextColor = ContextCompat.getColor(it, R.color.yellow)
                //fillColor = ContextCompat.getColor(it, R.color.light_blue)
            }

            valueTextSize = 12f
            setDrawValues(false)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawFilled(false)
            fillAlpha = 100
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val legend = lineChart.legend
        legend.textColor = textColor!!
        legend.textSize = 12f
        legend.form = Legend.LegendForm.LINE

        lineChart.axisLeft.textColor = textColor!!
        lineChart.axisLeft.gridColor = textColor as Int

        lineChart.axisRight.textColor = textColor as Int
        lineChart.axisRight.gridColor = textColor as Int
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.gridColor = Color.LTGRAY

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)


        val desc = Description()
        desc.text = "Grafik Tandon Air"
        lineChart.description = desc
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labelTanggal)
        lineChart.xAxis.granularity = 1f // penting agar label tidak tumpang tindih
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textColor = textColor as Int
        lineChart.xAxis.labelRotationAngle = -45f // miringkan label biar muat
        lineChart.xAxis.setDrawGridLines(false)


        lineChart.invalidate()
    }


    fun retrieveImage(){
        //START OF GET IMAGE
        val imageGraphSample2 = view?.findViewById<ImageView>(R.id.imageGrafikSampleTest)
        val imageGraphSample = view?.findViewById<ImageView>(R.id.imageGrafikSample)
        val URL: String = "https://abeazka.my.id/telemetri/tandongrafikbunda.php"
        if (URL.isNotEmpty()) {
            val http = OkHttpClient()
            val request = Request.Builder()
                .url(URL)
                .build()
            //myWebView.loadUrl(URL)

            http.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace();
                }

                override fun onResponse(call: Call, response: Response) {
                    val response: Response = http.newCall(request).execute()
                    val responseCode = response.code
                    val results = response.body?.string()

                    println("Success " + response.toString())
                    println("Success " + response.message.toString())
                    println("Success " + results)
                    Log.i("KODE", "CODE: " + responseCode)
                    Log.i("Response", "Received response from server. Response")
                    if (response.code == 200) {
                        //Thread.sleep(3_000)
                         // Adjust this delay to match your data loading time
                        println("GAMBAR BERHASIL DIBUILD")
                        println("TAHAP MUNCULIN GAMBAR.....")
                        //Popup

                        //Munculin Gambar
                        val handler = Handler(Looper.getMainLooper())
                        val URL2 = URL( "https://abeazka.my.id/telemetri/tandon/grafiktandon_bunda-x.php.png")
                        try{
                            val `in` = URL2.openStream()
                            image = BitmapFactory.decodeStream(`in`)
                            handler.post{
                                shimmerLayout.stopShimmer()
                                shimmerLayout.visibility = View.GONE // Hide shimmer after loading
                                imageGraphSample?.visibility = View.VISIBLE
                                //imageGrafik.setImageBitmap(image)

//                                var inflater = LayoutInflater.from(getActivity())
//                                var popupview = inflater.inflate(R.layout.popup_grafik, null,false)
//                                var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
//                                var close = popupview.findViewById<ImageView>(R.id.close)
//                                var builder = PopupWindow(popupview, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
//                                imagee.setImageBitmap(image)

                                if (imageGraphSample != null) {
                                    imageGraphSample.setImageBitmap(image)

                                }else{
                                    imageGraphSample?.setImageResource(R.drawable.sample)
                                }
                                //imagee.setRotation(90f)
//                                builder.setBackgroundDrawable(
//                                    AppCompatResources.getDrawable(
//                                        requireContext(),
//                                        R.drawable.background
//                                    )
//                                )
//                                builder.animationStyle=R.style.DialogAnimation
//                                builder.showAtLocation(getActivity()?.findViewById(R.id.drawer_layout), Gravity.CENTER, 0 ,0)
//                                close.setOnClickListener{
//                                    builder.dismiss()
//                                }

                            }


                        }catch (e:java.lang.Exception){
                            e.printStackTrace()
                        }





//                        getActivity()?.runOnUiThread {
//                            Toast.makeText(
//                                getActivity(),
//                                "Success",
//                                Toast.LENGTH_SHORT
//                            ).show();
//                        }
                    } else {
                        getActivity()?.runOnUiThread {

                            Log.e(
                                "HTTP Error",
                                "Something didn't load, or wasn't succesfully"
                            )
                            Toast.makeText(getActivity(), "Fail", Toast.LENGTH_LONG)
                                .show();

                        }
                        return
                    }
                }
            })
        }
    }

    fun retrieveData(){
//        pbData_BG!!.visibility = View.VISIBLE
        pbData?.visibility = View.VISIBLE

        val ardData: APIRequestData = RetroServer.konekRetrofit().create(APIRequestData::class.java)
        val tampilData: retrofit2.Call<ResponseModel> = ardData.ardRetrieveData()
        tampilData.enqueue(object: retrofit2.Callback<ResponseModel> {
            override fun onResponse(call: retrofit2.Call<ResponseModel>, response: retrofit2.Response<ResponseModel>) {

                text.visibility = View.GONE
                if(response.body()?.data == null){
                    text.visibility = View.VISIBLE
                    srlDat?.visibility = View.GONE
                    rvData?.visibility = View.GONE
//                    text.visibility = View.VISIBLE
                    pbData?.visibility = View.INVISIBLE
//                    pbData_BG!!.visibility = View.INVISIBLE
                }else{

                    text.visibility = View.GONE
                    srlDat?.visibility = View.VISIBLE
                    listData = response.body()?.data!!
                    adData = AdapterData(context, listData)
                    rvData?.smoothScrollToPosition(listData.size-1);
                    rvData?.visibility = View.VISIBLE

//                    text.visibility = View.GONE
                    rvData?.adapter = adData

                    adData?.notifyDataSetChanged()
//                    pbData_BG!!.visibility = View.INVISIBLE
                    pbData?.visibility = View.INVISIBLE
                }


            }

            override fun onFailure(call: retrofit2.Call<ResponseModel>, t: Throwable) {
                srlDat?.visibility = View.GONE
                text.visibility = View.VISIBLE
                getActivity()?.runOnUiThread {
                    Toast.makeText(
                        context, "Failed to connect: " + t.message, Toast.LENGTH_SHORT
                    ).show()
                }
                return
                Log.i("ERROR", "Failed to connect: " + t.message)
                pbData?.visibility = View.INVISIBLE
//                pbData_BG!!.visibility = View.INVISIBLE
            }

        })
    }
    private fun getUserData(): Map<String, String?> {
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isFirebase" to prefs.getString("isFirebase", null)
        )
    }



    fun retrieveTemp(){
        pbData?.visibility = View.VISIBLE

        val ardData: APIRequestData = RetroServer.konekRetrofit().create(APIRequestData::class.java)
        val tampilData: retrofit2.Call<ResponseModel> = ardData.ardRetrieveTemp()
        tampilData.enqueue(object: retrofit2.Callback<ResponseModel> {
            override fun onResponse(call: retrofit2.Call<ResponseModel>, response: retrofit2.Response<ResponseModel>) {
                val r = response.body()!!.data

                listLaundry = response.body()!!.data
                //text.visibility = View.GONE
                if(response.body()?.data != null){
//                    val dataset: BarDataSet = BarDataSet(yVals, r.get(0).);

                    var chanceRain = ""



                    if (listLaundry[r.lastIndex].kelembapan.toString() > "65"){
                        chanceRain = "Diperkirakan hujan/setelah hujan!"
                    }
                    else if(listLaundry[r.lastIndex].kelembapan.toString() == "100"){
                        chanceRain = "Sedang hujan!"
                    }
                    else{
                        chanceRain = "Diperkirakan tidak hujan!"
                    }

                    textHum.text = listLaundry[r.lastIndex].kelembapan.toString() + "%"
                    textTemo.text = listLaundry[r.lastIndex].suhu.toString() + "\u2103"
                    textTime.text = "Last update: "+ listLaundry[r.lastIndex].date.toString() + "\n $chanceRain"
                }
                //else{
//                    text.visibility = View.GONE
//                    srlDat!!.visibility = View.VISIBLE
//                    listData = response.body()!!.data
//                    adData = AdapterData(context, listData)
//                    rvData!!.smoothScrollToPosition(listData.size-1);
//                    rvData!!.visibility = View.VISIBLE
//
////                    text.visibility = View.GONE
//                    rvData!!.adapter = adData
//
//                    adData!!.notifyDataSetChanged()
////                    pbData_BG!!.visibility = View.INVISIBLE
//                    pbData!!.visibility = View.INVISIBLE
//                }


            }

            override fun onFailure(call: retrofit2.Call<ResponseModel>, t: Throwable) {
//                srlDat!!.visibility = View.GONE
//                text.visibility = View.VISIBLE
                getActivity()?.runOnUiThread {
                    Toast.makeText(
                        context, "Failed to connect: " + t.message, Toast.LENGTH_SHORT
                    ).show()
                }
                return
                Log.i("ERROR", "Failed to connect: " + t.message)
                pbData!!.visibility = View.INVISIBLE
//                pbData_BG!!.visibility = View.INVISIBLE
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}