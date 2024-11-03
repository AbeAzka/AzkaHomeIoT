package com.indodevstudio.azka_home_iot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.LineChart
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.APIRequestData
import com.indodevstudio.azka_home_iot.API.RetroServer
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
import java.util.Calendar


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
    var srlDat: ScrollView? = null
    var srlData: SwipeRefreshLayout? = null
    var pbData: ProgressBar? = null
    var cards2: CardView? = null
    var chart: LineChart? = null

    lateinit var textTime : TextView
    lateinit var textHum : TextView
    lateinit var textTemo : TextView


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

        rvData = view.findViewById(R.id.rv_data)
        srlData = view.findViewById(R.id.srl_data)
        pbData = view.findViewById(R.id.pb_data)
        text = view.findViewById(R.id.text_Inbox)
        srlDat = view.findViewById(R.id.srl_dta)
//        cards2 = view.findViewById(R.id.cards_info2)
//        text_card = view.findViewById(R.id.pp)
        textTime = view.findViewById(R.id.last_update)
        textHum = view.findViewById(R.id.humidity_txt)
        textTemo = view.findViewById(R.id.temperature_txt)
//        chart = view.findViewById(R.id.chart)
//        pbData_BG = view.findViewById(R.id.load)
         val imageGraphSample = view.findViewById<ImageView>(R.id.imageGrafikSample)

        lmData = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        (lmData as LinearLayoutManager).setReverseLayout(true);
        (lmData as LinearLayoutManager).setStackFromEnd(true);
        with(rvData){this?.setLayoutManager(lmData)}

        with (srlData){
            this?.setOnRefreshListener {
                setRefreshing(true)
                retrieveData()
//                getNotice()
                retrieveTemp()
                retrieveImage()
                setRefreshing(false)

            }
        }
        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val greeting = view.findViewById<TextView>(R.id.greetings)

        val displayName = getArguments()?.getString("name")

//        val imageGraphSample2 = view.findViewById<ImageView>(R.id.imageGrafikSampleHehe)
        val namee = user!!.displayName


//        //START OF GET IMAGE
//        val URL: String = "https://abeazka.my.id/telemetri/tandongrafikbunda.php"
//        if (URL.isNotEmpty()) {
//            val http = OkHttpClient()
//            val request = Request.Builder()
//                .url(URL)
//                .build()
//            //myWebView.loadUrl(URL)
//
//            http.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    e.printStackTrace();
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val response: Response = http.newCall(request).execute()
//                    val responseCode = response.code
//                    val results = response.body!!.string()
//
//                    println("Success " + response.toString())
//                    println("Success " + response.message.toString())
//                    println("Success " + results)
//                    Log.i("KODE", "CODE: " + responseCode)
//                    Log.i("Response", "Received response from server. Response")
//                    if (response.code == 200) {
//                        //Thread.sleep(3_000)
//                        println("GAMBAR BERHASIL DIBUILD")
//                        println("TAHAP MUNCULIN GAMBAR.....")
//                        //Popup
//
//                        //Munculin Gambar
//                        val handler = Handler(Looper.getMainLooper())
//                        val URL2 = URL( "https://abeazka.my.id/telemetri/tandon/grafiktandon_bunda-x.php.png")
//                        try{
//                            val `in` = URL2.openStream()
//                            image = BitmapFactory.decodeStream(`in`)
//                            handler.post{
//                                //imageGrafik.setImageBitmap(image)
//
////                                var inflater = LayoutInflater.from(getActivity())
////                                var popupview = inflater.inflate(R.layout.popup_grafik, null,false)
////                                var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
////                                var close = popupview.findViewById<ImageView>(R.id.close)
////                                var builder = PopupWindow(popupview, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
////                                imagee.setImageBitmap(image)
//
//                                imageGraphSample.setImageBitmap(image)
//                                //imagee.setRotation(90f)
////                                builder.setBackgroundDrawable(
////                                    AppCompatResources.getDrawable(
////                                        requireContext(),
////                                        R.drawable.background
////                                    )
////                                )
////                                builder.animationStyle=R.style.DialogAnimation
////                                builder.showAtLocation(getActivity()?.findViewById(R.id.drawer_layout), Gravity.CENTER, 0 ,0)
////                                close.setOnClickListener{
////                                    builder.dismiss()
////                                }
//
//                            }
//
//
//                        }catch (e:java.lang.Exception){
//                            e.printStackTrace()
//                        }
//
//
//
//
//
////                        getActivity()?.runOnUiThread {
////                            Toast.makeText(
////                                getActivity(),
////                                "Success",
////                                Toast.LENGTH_SHORT
////                            ).show();
////                        }
//                    } else {
//                        getActivity()?.runOnUiThread {
//
//                            Log.e(
//                                "HTTP Error",
//                                "Something didn't load, or wasn't succesfully"
//                            )
//                            Toast.makeText(getActivity(), "Fail", Toast.LENGTH_LONG)
//                                .show();
//
//                        }
//                        return
//                    }
//                }
//            })
//        }

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
        val email2 = user!!.email


//        println("Current Date is: $formatted")


        return view
    }
    override fun onResume(){
        super.onResume()
        retrieveData()
        retrieveTemp()
        retrieveImage()
    }



//    fun getNotice(){
//        val URL: String = "https://abeazka.my.id/arduino_keypad/tes.txt"
//        if (URL.isNotEmpty()) {
//            val http = OkHttpClient()
//            val request = Request.Builder()
//                .url(URL)
//                .build()
//            //myWebView.loadUrl(URL)
//
//            http.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    e.printStackTrace();
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val response: Response = http.newCall(request).execute()
//                    val responseCode = response.code
//                    val results = response.body!!.string()
//
//                    println("Success " + response.toString())
//                    println("Success " + response.message.toString())
//                    println("Success " + results)
//                    Log.i("KODE", "CODE: " + responseCode)
//                    Log.i("Response", "Received response from server. Response")
//                    if (response.code == 200) {
//                        Thread.sleep(3_000)
//                        println("MENERIMA PESAN")
//                        println("TAHAP MUNCULIN PESAN.....")
//
//                        val content = StringBuilder()
//                        try{
//                            val url = URL("https://abeazka.my.id/arduino_keypad/tes.txt")
//                            val urlConnection = url.openConnection()
//                            val bufferedReader =
//                                BufferedReader(InputStreamReader(urlConnection.getInputStream()))
//                            var line: String?
////                            while (bufferedReader.readLine().also { line = it } != null) {
////                                content.append(line).append("\n")
////                            }
//
//                                if(content.toString().isNullOrBlank() || content.toString().isNullOrEmpty()){
//                                    cards2!!.visibility = View.VISIBLE
//                                    text_card.text = content.toString()
//                                }else{
//                                    cards2!!.visibility = View.GONE
//                                }
//
//                            bufferedReader.close()
//
//
//
////                            BufferedReader(InputStreamReader(u.openStream())).use { r ->
////                                text_card.text = r.lines().toString()
////                            }
//
////                            val `in` = URL2.openStream()
////                            image = BitmapFactory.decodeStream(`in`)
////                            handler.post{
////
////
////                            }
//
//
//                        }catch (e:java.lang.Exception){
//                            e.printStackTrace()
//                        }
//                    } else {
//                        getActivity()?.runOnUiThread {
//
//                            Log.e(
//                                "HTTP Error",
//                                "Something didn't load, or wasn't succesfully"
//                            )
//                            Toast.makeText(getActivity(), "Fail", Toast.LENGTH_LONG)
//                                .show();
//
//                        }
//                        return
//                    }
//                }
//            })
//        }
//    }

    fun retrieveImage(){
        //START OF GET IMAGE
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
                    val results = response.body!!.string()

                    println("Success " + response.toString())
                    println("Success " + response.message.toString())
                    println("Success " + results)
                    Log.i("KODE", "CODE: " + responseCode)
                    Log.i("Response", "Received response from server. Response")
                    if (response.code == 200) {
                        //Thread.sleep(3_000)
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
                                //imageGrafik.setImageBitmap(image)

//                                var inflater = LayoutInflater.from(getActivity())
//                                var popupview = inflater.inflate(R.layout.popup_grafik, null,false)
//                                var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
//                                var close = popupview.findViewById<ImageView>(R.id.close)
//                                var builder = PopupWindow(popupview, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
//                                imagee.setImageBitmap(image)

                                if (imageGraphSample != null) {
                                    imageGraphSample.setImageBitmap(image)
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
        pbData!!.visibility = View.VISIBLE

        val ardData: APIRequestData = RetroServer.konekRetrofit().create(APIRequestData::class.java)
        val tampilData: retrofit2.Call<ResponseModel> = ardData.ardRetrieveData()
        tampilData.enqueue(object: retrofit2.Callback<ResponseModel> {
            override fun onResponse(call: retrofit2.Call<ResponseModel>, response: retrofit2.Response<ResponseModel>) {

                text.visibility = View.GONE
                if(response.body()?.data == null){
                    text.visibility = View.VISIBLE
                    srlDat!!.visibility = View.GONE
                    rvData!!.visibility = View.GONE
//                    text.visibility = View.VISIBLE
                    pbData!!.visibility = View.INVISIBLE
//                    pbData_BG!!.visibility = View.INVISIBLE
                }else{
                    text.visibility = View.GONE
                    srlDat!!.visibility = View.VISIBLE
                    listData = response.body()!!.data
                    adData = AdapterData(context, listData)
                    rvData!!.smoothScrollToPosition(listData.size-1);
                    rvData!!.visibility = View.VISIBLE

//                    text.visibility = View.GONE
                    rvData!!.adapter = adData

                    adData!!.notifyDataSetChanged()
//                    pbData_BG!!.visibility = View.INVISIBLE
                    pbData!!.visibility = View.INVISIBLE
                }


            }

            override fun onFailure(call: retrofit2.Call<ResponseModel>, t: Throwable) {
                srlDat!!.visibility = View.GONE
                text.visibility = View.VISIBLE
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


    fun retrieveTemp(){
        pbData!!.visibility = View.VISIBLE

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
                        chanceRain = "Berpotensi hujan!"
                    }else{
                        chanceRain = "Berpotensi tidak hujan!"
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