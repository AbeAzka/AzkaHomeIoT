package com.indodevstudio.azka_home_iot

import ApiService
import CreditRequest
import CreditResponse
import DailyGet
import DebitRequest
import DeleteAllResponse
import HistoryResponse
import MonthlyGet
import Server
import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.ImageCapture
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.Adapter.HistoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinansialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */



class FinansialFragment : Fragment() {
    // TODO: Rename and change types of parameters
    var email = ""
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var valueInput1: EditText
    private lateinit var descInput1: EditText
    private lateinit var submitCredit1: Button
    private lateinit var submitDebit1: Button
    private lateinit var deleteAllData: Button
    private lateinit var creditValueDisplay1: TextView
    private lateinit var cardView1: CardView

    private lateinit var valueInput2: EditText
    private lateinit var descInput2: EditText
    private lateinit var submitCredit2: Button
    private lateinit var submitDebit2: Button
    private lateinit var creditValueDisplay2: TextView
    private lateinit var cardView2: CardView

    private lateinit var valueInput3: EditText
    private lateinit var descInput3: EditText
    private lateinit var submitCredit3: Button
    private lateinit var submitDebit3: Button
    private lateinit var creditValueDisplay3: TextView
    private lateinit var cardView3: CardView

    private lateinit var valueInput4: EditText
    private lateinit var descInput4: EditText
    private lateinit var submitCredit4: Button
    private lateinit var submitDebit4: Button
    private lateinit var creditValueDisplay4: TextView
    private lateinit var cardView4: CardView

    private lateinit var valueInput5: EditText
    private lateinit var descInput5: EditText
    private lateinit var submitCredit5: Button
    private lateinit var submitDebit5: Button
    private lateinit var creditValueDisplay5: TextView
    private lateinit var cardView5: CardView

    private lateinit var valueInput6: EditText
    private lateinit var descInput6: EditText
    private lateinit var submitCredit6: Button
    private lateinit var submitDebit6: Button
    private lateinit var creditValueDisplay6: TextView
    private lateinit var cardView6: CardView

    private lateinit var valueInputTest: EditText
    private lateinit var descInputTest: EditText
    private lateinit var submitCreditTest: Button
    private lateinit var submitDebitTest: Button
    private lateinit var creditValueDisplayTest: TextView
    private lateinit var cardViewTest: CardView
    private lateinit var cardTextTitle: TextView

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var billBtn : Button
    private val REQUEST_IMAGE_CAPTURE = 100
//    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    private lateinit var harianTxt : TextView
    private lateinit var monthlyTxt : TextView
    private lateinit var expendTxt : TextView

    private val CHANNEL_ID = "progress_channel"
    private val NOTIFICATION_ID = 10
    private val NOTIFICATION_ID2 = 12
    private lateinit var historyBtn: Button
    lateinit var shimmerFrame : ShimmerFrameLayout
    private lateinit var spinner: Spinner
    private val api = Server.instance.create(ApiService::class.java)
    val userTokens = mapOf(
        "bensin_x" to "xujaTb51bh",
        "sabun" to "3MNWbZwEdY",
        "jajan" to "l7DXScUPSK",
        "makan" to "ZizaZCZz6W",
        "ss" to "tZ2sAr9Jyx",
        "cadangan" to "GTcypTZSuP"
    )
    private var emailU = ""
    private var tokenS = ""
    private val list = ArrayList<HistoryResponse>()
    private lateinit var recyclerView : RecyclerView
    private lateinit var headerTable : LinearLayout
    var bensin = false
    var kebutuhan = false
    var srlData: SwipeRefreshLayout? = null
    var pbData: ProgressBar? = null

    //private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var imageCapture: ImageCapture

    //private lateinit var imgThumbnail: ImageView


    private lateinit var countdownTimer: CountDownTimer
    private val countdownTimeInMillis: Long = 5000      // 1 hour and 1 minute (for testing purposes)
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
        val view =  inflater.inflate(R.layout.fragment_finansial, container, false)

        //previewView = view.findViewById(R.id.previewView)

        //imgThumbnail = view.findViewById(R.id.imgThumbnail)

        valueInput1 = view.findViewById(R.id.creditInput)
        descInput1 = view.findViewById(R.id.inputDsc)
        submitCredit1 = view.findViewById(R.id.submitCredit)
        submitDebit1 = view.findViewById(R.id.submitDebit)
        deleteAllData = view.findViewById(R.id.deleteData)
        creditValueDisplay1 = view.findViewById(R.id.creditValueDisplay)
        cardView1 = view.findViewById(R.id.cards_info)


        valueInput2 = view.findViewById(R.id.creditInput2)
        descInput2 = view.findViewById(R.id.inputDsc2)
        submitCredit2= view.findViewById(R.id.submitCredit2)
        submitDebit2 = view.findViewById(R.id.submitDebit2)
        creditValueDisplay2 = view.findViewById(R.id.creditValueDisplay2)
        cardView2 = view.findViewById(R.id.cards_info2)

        spinner = view.findViewById(R.id.mbuh)

        valueInput3 = view.findViewById(R.id.creditInput3)
        descInput3 = view.findViewById(R.id.inputDsc3)
        submitCredit3= view.findViewById(R.id.submitCredit3)
        submitDebit3 = view.findViewById(R.id.submitDebit3)
        creditValueDisplay3 = view.findViewById(R.id.creditValueDisplay3)
        cardView3 = view.findViewById(R.id.cards_info3)

        valueInput4 = view.findViewById(R.id.creditInput4)
        descInput4 = view.findViewById(R.id.inputDsc4)
        submitCredit4= view.findViewById(R.id.submitCredit4)
        submitDebit4 = view.findViewById(R.id.submitDebit4)
        creditValueDisplay4 = view.findViewById(R.id.creditValueDisplay4)
        cardView4 = view.findViewById(R.id.cards_info4)

        valueInput5 = view.findViewById(R.id.creditInput5)
        descInput5 = view.findViewById(R.id.inputDsc5)
        submitCredit5= view.findViewById(R.id.submitCredit5)
        submitDebit5 = view.findViewById(R.id.submitDebit5)
        creditValueDisplay5 = view.findViewById(R.id.creditValueDisplay5)
        cardView5 = view.findViewById(R.id.cards_info5)

        valueInput6 = view.findViewById(R.id.creditInput6)
        descInput6 = view.findViewById(R.id.inputDsc6)
        submitCredit6= view.findViewById(R.id.submitCredit6)
        submitDebit6 = view.findViewById(R.id.submitDebit6)
        creditValueDisplay6 = view.findViewById(R.id.creditValueDisplay6)
        cardView6 = view.findViewById(R.id.cards_info6)

        valueInputTest = view.findViewById(R.id.creditInput6)
        descInputTest = view.findViewById(R.id.inputDsc6)
        submitCreditTest= view.findViewById(R.id.submitCredit6)
        submitDebitTest = view.findViewById(R.id.submitDebit6)
        creditValueDisplayTest = view.findViewById(R.id.creditValueDisplay6)
        cardViewTest = view.findViewById(R.id.cards_info6)
        cardTextTitle = view.findViewById(R.id.txTest)

        billBtn = view.findViewById(R.id.takePhoto)
        historyBtn = view.findViewById(R.id.showTable)
        srlData = view.findViewById(R.id.srl_data)
        pbData = view.findViewById(R.id.pb_data)
        shimmerFrame = view.findViewById(R.id.shimmerLayout2);

        harianTxt = view.findViewById(R.id.harian_Txt)
        monthlyTxt = view.findViewById(R.id.bulanan_Txt)
        expendTxt = view.findViewById(R.id.titleDaily)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val userData = getUserData()

        if(firebaseUser != null){
            email = FirebaseAuth.getInstance().currentUser?.email.toString()

        }

        if(authToken != null) {
            email = userData["email"].toString()

        }



        context?.let { createNotificationChannel(it) }
        var inflater = LayoutInflater.from(getActivity())
        var popupview =
            inflater.inflate(R.layout.popup_grafik2, null, false)
        var buttonDownload =
            popupview.findViewById<Button>(R.id.downloadPdf)




        emailU = FirebaseAuth.getInstance().currentUser?.displayName.toString()

//        headerTable = view.findViewById(R.id.recyclerView2_table)

//        recyclerView = view.findViewById(R.id.recyclerView2)
//        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)


        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.boolean_options,
            android.R.layout.simple_spinner_dropdown_item
        )


        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Set the default selection (e.g., select the first item - "Kebutuhan")
        val defaultSelection = 0 // Select "Bensin"
        spinner.setSelection(defaultSelection)


        // Set an ItemSelectedListener to handle the user's selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()

                when (val selectedItem = spinner.selectedItem.toString()) {
                    "Bensin_Xenia" -> {
                        // Action for "Kebutuhan"
                        val bensinx = userTokens["bensin_x"].toString()
                        GetExpendDaily(bensinx)
                        GetExpendMonthly(bensinx)
                            fetchCreditData(bensinx)
                        tokenS = userTokens["bensin_x"].toString()


                        cardView1.visibility = View.VISIBLE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.GONE
                        cardView4.visibility = View.GONE
                        cardView5.visibility = View.GONE
                        cardView6.visibility = View.GONE



                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Kebutuhan_Alat_Mandi" -> {
                        // Action for "Bensin"
                        val sabun = userTokens["sabun"].toString()
                        GetExpendDaily(sabun)
                        GetExpendMonthly(sabun)
                        tokenS = userTokens["sabun"].toString()
                        fetchCreditData2(sabun)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.VISIBLE
                        cardView3.visibility = View.GONE
                        cardView4.visibility = View.GONE
                        cardView5.visibility = View.GONE
                        cardView6.visibility = View.GONE
                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Jajan_Anak" -> {
                        // Action for "Bensin"
                        val jajan = userTokens["jajan"].toString()
                        tokenS = userTokens["jajan"].toString()
                        GetExpendDaily(jajan)
                        GetExpendMonthly(jajan)
                        fetchCreditData3(jajan)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.VISIBLE
                        cardView4.visibility = View.GONE
                        cardView5.visibility = View.GONE
                        cardView6.visibility = View.GONE
                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Makan_Bunda_Di_Kantor" -> {
                        // Action for "Bensin"
                        val makan = userTokens["makan"].toString()
                        tokenS = userTokens["makan"].toString()
                        GetExpendDaily(makan)
                        GetExpendMonthly(makan)
                        fetchCreditData4(makan)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.GONE
                        cardView4.visibility = View.VISIBLE
                        cardView5.visibility = View.GONE
                        cardView6.visibility = View.GONE
                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Kebutuhan_Sarapan_dan_Sayuran" -> {
                        // Action for "Bensin"
                        val ss = userTokens["ss"].toString()
                        tokenS = userTokens["ss"].toString()
                        fetchCreditData5(ss)
                        GetExpendDaily(ss)
                        GetExpendMonthly(ss)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.GONE
                        cardView4.visibility = View.GONE
                        cardView5.visibility = View.VISIBLE
                        cardView6.visibility = View.GONE
                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Kebutuhan_Cadangan_Bunda" -> {
                        // Action for "Bensin"
                        val cadangan = userTokens["cadangan"].toString()
                        GetExpendDaily(cadangan)
                        GetExpendMonthly(cadangan)
                        tokenS = userTokens["cadangan"].toString()
                        fetchCreditData6(cadangan)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.GONE
                        cardView4.visibility = View.GONE
                        cardView5.visibility = View.GONE
                        cardView6.visibility = View.VISIBLE
                        //Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Action for other selections, if any
                    }
                }
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected (optional)
            }
        }

        with (srlData){
            this?.setOnRefreshListener {
                setRefreshing(true)
                val bensinx = userTokens["bensin_x"].toString()
                val sabun = userTokens["sabun"].toString()
                val jajan = userTokens["jajan"].toString()
                val makan = userTokens["makan"].toString()
                val ss = userTokens["ss"].toString()
                val cadangan = userTokens["cadangan"].toString()

                if(tokenS == "xujaTb51bh") {
                    fetchCreditData(bensinx)
                }
                else if(tokenS == "3MNWbZwEdY") {
                    fetchCreditData2(sabun)
                }else if(tokenS == "l7DXScUPSK") {
                    fetchCreditData3(jajan)
                }else if(tokenS == "ZizaZCZz6W") {
                    fetchCreditData4(makan)
                }else if(tokenS == "tZ2sAr9Jyx") {
                    fetchCreditData5(ss)
                }else if(tokenS == "GTcypTZSuP") {
                    fetchCreditData6(cadangan)
                }







                setRefreshing(false)

            }
        }


        startCountdown(deleteAllData)
// Set up button click listeners
        historyBtn.setOnClickListener{
//            pdf1(tokenS)
            Server.setToken(tokenS)
            var textt = ""
            if(tokenS == "xujaTb51bh") {
                textt = "Bensin Xenia"
            }
            else if(tokenS == "3MNWbZwEdY") {
                textt = "Kebutuhan Alat Mandi"
            }else if(tokenS == "l7DXScUPSK") {
                textt = "Jajan Anak-Anak"
            }else if(tokenS == "ZizaZCZz6W") {
                textt = "Makan Bunda Di Kantor"
            }else if(tokenS == "tZ2sAr9Jyx") {
                textt = "Kebutuhan Sarapan dan Sayuran"
            }else if(tokenS == "GTcypTZSuP") {
                textt = "Kebutuhan Cadangan Bunda"
            }
            api.getHistory().enqueue(object : Callback<ArrayList<HistoryResponse>>{
                override fun onResponse(
                    call: Call<ArrayList<HistoryResponse>>,
                    response: Response<ArrayList<HistoryResponse>>
                ) {

                    val responseCode = response.code().toString()
                    if(response.body() != null) {
                        response.body()?.let { list.addAll(it) }
                        val adapter = HistoryAdapter(list)
//                recyclerView.adapter = adapter

                        var inflater = LayoutInflater.from(getActivity())
                        var popupview =
                            inflater.inflate(R.layout.popup_grafik2, null, false)
                        var recycler =
                            popupview.findViewById<RecyclerView>(R.id.recyclerView2)
                        var close =
                            popupview.findViewById<ImageView>(R.id.close)
                        var builder = PopupWindow(
                            popupview,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            true
                        )

                        val  btn = popupview.findViewById<Button>(R.id.downloadPdf)

                        btn.setOnClickListener{
                            if(tokenS == "xujaTb51bh") {

                                try {
                                    downloadPdf(tokenS, "AHI-bensin_x")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }
                            else if(tokenS == "3MNWbZwEdY") {
                                try {
                                    downloadPdf(tokenS, "AHI-sabun")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }else if(tokenS == "l7DXScUPSK") {
                                try {
                                    downloadPdf(tokenS, "AHI-jajan")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }else if(tokenS == "ZizaZCZz6W") {
                                try {
                                    downloadPdf(tokenS, "AHI-makan_b")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }else if(tokenS == "tZ2sAr9Jyx") {
                                try {
                                    downloadPdf(tokenS, "AHI-ss")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }else if(tokenS == "GTcypTZSuP") {
                                try {
                                    downloadPdf(tokenS, "AHI-cadangan")
                                    Log.i("Retro","PDF downloaded successfully")
                                } catch (e: Exception) {
                                    Log.i("Retro","Error downloading PDF: ${e.message}")
                                }
                            }

                        }
                        val txt = popupview.findViewById<TextView>(R.id.idsss)
                        txt.text = "Data History \n$textt"
                        recycler.layoutManager =
                            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                        recycler.adapter = adapter
                        adapter.notifyDataSetChanged()


                        recycler.removeAllViewsInLayout()

                        //imagee.setRotation(90f)
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

                            recycler.removeAllViewsInLayout()
                            adapter.clearData()
                        }
                    }else{
//                        Toast.makeText(activity, "No Data Available", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onFailure(call: Call<ArrayList<HistoryResponse>>, t: Throwable) {
                    Log.e("retro", "Error: ${t.message}")
                    Toast.makeText(activity, "Data Not Available", Toast.LENGTH_SHORT).show()
                }

            })
        }



        deleteAllData.setOnClickListener{

            showConfirmationDialog()
        }
        submitCredit1.setOnClickListener {
            val creditValue = valueInput1.text.toString()
            val msg = descInput1.text.toString()
            if (creditValue != null) {
                val bensinx = userTokens["bensin_x"].toString()
                postCredit(creditValue,msg, bensinx, emailU)
                valueInput1.text.clear()
                descInput1.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit1.setOnClickListener {
            val debitValue = valueInput1.text.toString()
            if (debitValue != null) {
                val msg = descInput1.text.toString()
                val bensinx = userTokens["bensin_x"].toString()
                postDebit(debitValue, msg, bensinx, emailU)
                valueInput1.text.clear()
                descInput1.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit2.setOnClickListener {
            val creditValue = valueInput2.text.toString()
            val msg = descInput2.text.toString()
            if (creditValue != null) {
                val sabun = userTokens["sabun"].toString()
                postCredit(creditValue,msg, sabun, emailU)
                valueInput2.text.clear()
                descInput2.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit2.setOnClickListener {
            val debitValue = valueInput2.text.toString()
            if (debitValue != null) {
                val msg = descInput2.text.toString()
                val sabun = userTokens["sabun"].toString()
                postDebit(debitValue, msg, sabun, emailU)
                valueInput2.text.clear()
                descInput2.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit3.setOnClickListener {
            val creditValue = valueInput3.text.toString()
            val msg = descInput3.text.toString()
            if (creditValue != null) {
                val jajan = userTokens["jajan"].toString()
                postCredit(creditValue,msg, jajan, emailU)
                valueInput3.text.clear()
                descInput3.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit3.setOnClickListener {
            val debitValue = valueInput3.text.toString()
            if (debitValue != null) {
                val msg = descInput3.text.toString()
                val jajan = userTokens["jajan"].toString()
                postDebit(debitValue, msg, jajan, emailU)
                valueInput3.text.clear()
                descInput3.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit4.setOnClickListener {
            val creditValue = valueInput4.text.toString()
            val msg = descInput4.text.toString()
            if (creditValue != null) {
                val makan = userTokens["makan"].toString()
                postCredit(creditValue,msg, makan, emailU)
                valueInput4.text.clear()
                descInput4.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit4.setOnClickListener {
            val debitValue = valueInput4.text.toString()
            if (debitValue != null) {
                val msg = descInput4.text.toString()
                val makan = userTokens["makan"].toString()
                postDebit(debitValue, msg, makan, emailU)
                valueInput4.text.clear()
                descInput4.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit5.setOnClickListener {
            val creditValue = valueInput5.text.toString()
            val msg = descInput5.text.toString()
            if (creditValue != null) {
                val ss = userTokens["ss"].toString()
                postCredit(creditValue,msg, ss, emailU)
                valueInput5.text.clear()
                descInput5.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit5.setOnClickListener {
            val debitValue = valueInput5.text.toString()
            if (debitValue != null) {
                val msg = descInput5.text.toString()
                val ss = userTokens["ss"].toString()
                postDebit(debitValue, msg, ss, emailU)
                valueInput5.text.clear()
                descInput5.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit6.setOnClickListener {
            val creditValue = valueInput6.text.toString()
            val msg = descInput6.text.toString()
            if (creditValue != null) {
                val cadangan = userTokens["cadangan"].toString()
                postCredit(creditValue,msg, cadangan, emailU)
                valueInput6.text.clear()
                descInput6.text.clear()
            } else {
                Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitDebit6.setOnClickListener {
            val debitValue = valueInput6.text.toString()
            if (debitValue != null) {
                val msg = descInput6.text.toString()
                val cadangan = userTokens["cadangan"].toString()
                postDebit(debitValue, msg, cadangan, emailU)
                valueInput6.text.clear()
                descInput6.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        billBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.CAMERA), 1
                )
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CameraFragment()) // Gantilah dengan ID container yang benar
                    .addToBackStack(null)
                    .commit()
            }
        }
        /*billBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    1
                )
            }
            dispatchTakePictureIntent()
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(
//                intent,
//                REQUEST_IMAGE_CAPTURE
//            )

        }*/

        return view
    }

//    private fun sharePDF() {
//        val shareIntent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, getContext()?.let {
//                FileProvider.getUriForFile(
//                    it, "com.indodevstudio.azka_home_iot.fileprovider",
//
//                )
//            })
//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            type = "application/pdf"
//        }
//        val sendIntent = Intent.createChooser(shareIntent, null)
//        startActivity(sendIntent)
//    }

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



    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE  )
        try {
            activity?.startActivityFromFragment(this,takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            // display error state(camera app not found) to the user
            Toast.makeText(requireContext(), "ERROR!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imagePath = data?.getStringExtra("image_path")
            if (imagePath != null) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                //imgThumbnail.setImageBitmap(bitmap)
                uploadPhoto(bitmap) // Upload ke server
            }
        }
    }

    private fun uploadPhoto(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val photoPart = MultipartBody.Part.createFormData("photo", "bill.jpg", requestBody)

        api.uploadPhoto(photoPart).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadPhoto(file: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            val requestBody = file.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", "bill.jpg", requestBody)

            api.uploadPhoto(photoPart).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    fun saveToDownloads(context: Context, fileName: String, urlString: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "download_channel"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "File Download",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_)
            .setContentTitle("Mengunduh $fileName")
            .setContentText("0%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)

        notificationManager.notify(notificationId, builder.build())

        Thread {
            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.connect()

                val lengthOfFile = connection.contentLength
                val input = BufferedInputStream(url.openStream())
                val outputFile: File
                val uri: Uri

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val newUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    uri = newUri!!
                    resolver.openOutputStream(uri)!!.use { output ->
                        val data = ByteArray(1024)
                        var total: Long = 0
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            output.write(data, 0, count)
                            val progress = ((total * 100) / lengthOfFile).toInt()
                            builder.setProgress(100, progress, false)
                                .setContentText("$progress%")
                            notificationManager.notify(notificationId, builder.build())
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    outputFile = File(downloadsDir, fileName)
                    val output = FileOutputStream(outputFile)
                    val data = ByteArray(1024)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        output.write(data, 0, count)
                        val progress = ((total * 100) / lengthOfFile).toInt()
                        builder.setProgress(100, progress, false)
                            .setContentText("$progress%")
                        notificationManager.notify(notificationId, builder.build())
                    }
                    output.flush()
                    output.close()

                    uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        outputFile
                    )
                }

                input.close()

                builder.setContentText("Download selesai")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                notificationManager.notify(notificationId, builder.build())

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val uriFolder = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    downloadsDir
                )

                val openFolderIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uriFolder, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(openFolderIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "File Manager tidak tersedia untuk membuka folder", Toast.LENGTH_LONG).show()
                }




            } catch (e: Exception) {
                Log.e("DownloadError", "Error saat download: ${e.message}", e)
                builder.setContentText("Download gagal: ${e.message}")
                    .setProgress(0, 0, false)
                notificationManager.notify(notificationId, builder.build())
            }
        }.start()
    }


    fun downloadPdf(token: String, name: String ){
        val URL : String ="https://www.indodevstudio.my.id/api/json/finansial/generate_pdf.php"

        //myWebView.loadUrl("http://taryem.my.id/Lab01/labx.php?type=on")
        //myWebView.loadUrl("http://taryem.my.id/Lab01/labx.php?type=on")
        if (URL.isNotEmpty()){
            val http = OkHttpClient()
            val request = Request.Builder()
                .url(URL)
                .header("Authorization", "Bearer $token")
                .build()
            //myWebView.loadUrl(URL)

            http.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace();
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val response: okhttp3.Response = http.newCall(request).execute()
                    val responseCode = response.code
                    val results = response.body!!.string()

                    println("Success " + response.toString())
                    println("Success " + response.message.toString())
                    println("Success " + results)
                    Log.i("KODE", "CODE: "+ responseCode)
                    Log.i("Response", "Received response from server. Response")
                    if (response.code == 200){
                        Log.i("retro", "Sukses generate pdf")


                            val URL2 = URL("https://www.indodevstudio.my.id/api/json/finansial/pdf_file/$name.pdf")

                        if (URL2.toString().isNotEmpty()) {
                            val http = OkHttpClient()
                            val request = Request.Builder()
                                .url(URL2)
                                .build()
                            http.newCall(request).enqueue(object : okhttp3.Callback {
                                override fun onFailure(call: okhttp3.Call, e: IOException) {
                                    e.printStackTrace();
                                }

                                override fun onResponse(
                                    call: okhttp3.Call,
                                    response: okhttp3.Response
                                ) {
                                    if (response.code == 200) {
                                        val inputStream = URL2.openStream()
                                        getActivity()?.runOnUiThread {
                                            Toast.makeText(
                                                getActivity(),
                                                "Download File....",
                                                Toast.LENGTH_LONG
                                            ).show();
                                        }
//                                        context?.let { showDownloadingNotification(it, 100) }

                                        saveToDownloads(requireContext(), "$name.pdf", "https://www.indodevstudio.my.id/api/finansial/pdf_file/$name.pdf")




                                    }else{
                                        getActivity()?.runOnUiThread {
                                            Toast.makeText(
                                                getActivity(),
                                                "Failed to download pdf",
                                                Toast.LENGTH_LONG
                                            ).show();
                                        }

                                    }
                                }

                            })
                        }

                    }else{
                        getActivity()?.runOnUiThread {

                            Log.e(
                                "HTTP Error",
                                "Something didn't load, or wasn't succesfully"
                            )
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();

                        }
                        return
                    }
                }
            })
        }
    }

    fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Progress Channel"
            val descriptionText = "Channel untuk notifikasi proses download"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

//    private fun buildProgressNotification(progress: Int): Notification {
//        return NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Progress Notification")
//            .setContentText("Downloading... $progress%")
//            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
//            .setProgress(100, progress, false) // max value, current value, indeterminate (false)
//            .setOngoing(true)
//            .build()
//    }

    fun showDownloadingNotification(context: Context, maxProgress: Int) {
        val notificationId = 1
        val progress = 0
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the initial notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
            .setContentTitle("Download File")
            .setContentText("Download in progress")
            .setProgress(maxProgress, 0, false) // Set initial progress
            .setOngoing(true) // Makes the notification persistent

        // Display the initial notification
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        // Update progress using a coroutine
//        CoroutineScope(Dispatchers.IO).launch {
//            for (progress in 1..maxProgress) {
//                delay(100) // Simulate download time
//
//                // Update progress
//                builder.setProgress(maxProgress, progress, false)
//
//                withContext(Dispatchers.Main) { // Update on the main thread
//                    notificationManager.notify(NOTIFICATION_ID, builder.build())
//                }
//            }
//
//            // Once download is complete, update the notification
//            withContext(Dispatchers.Main) { // Final update on the main thread
////                builder.setContentText("Download complete")
////                    .setContentText("Downloading... $progress%")
////                    .setProgress(maxProgress, progress, false) // Remove progress bar
////                    .setOngoing(false) // Allow dismissal
////                notificationManager.notify(NOTIFICATION_ID, builder.build())
//                val completedNotification = showDownloadingNotification()
//                notificationManager.notify(NOTIFICATION_ID, completedNotification)
//            }
//        }


    }

    private fun buildProgressNotification(progress: Int): Notification {
        return NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Download in Progress")
            .setContentText("Downloading... $progress%")
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
            .setProgress(100, progress, false) // Max value = 100, current progress
            .setOngoing(true) // Keeps the notification active
            .build()
    }

    fun showDownloadingNotification(): Notification  {
        return NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("The file has been successfully downloaded.")
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
            .setAutoCancel(true) // Automatically dismiss the notification when clicked
            .build()
//        val notificationId = 1
//        val progress = 0
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Build the initial notification
//        val builder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
//            .setContentTitle("Download Complete")
//            .setContentText("The file has been successfully downloaded.")
//            .setAutoCancel(true) // Dismiss notification on click
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        notificationManager.notify(NOTIFICATION_ID, completedNotification)
    }

    private fun buildCompletedNotification(): Notification {
        return NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("The file has been successfully downloaded.")
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
            .setAutoCancel(true) // Dismiss the notification when tapped
            .build()
    }

    fun showNotification(context: Context, title: String, description: String) {
        val channelId = "id_1"
        val notificationId = 5

        val intent = Intent(context, FinansialFragment::class.java) // Activity to open on click
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.azkahomeiot__288_x_288_pixel_) // Replace with your icon
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true) // Dismiss notification on click
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }


    private fun GetExpendDaily(token: String){
        Server.setToken(token)
        api.getDaily().enqueue(object : Callback<DailyGet>{
            override fun onResponse(call: Call<DailyGet>, response: Response<DailyGet>) {
                if (response.isSuccessful){
                    if(token == "xujaTb51bh") {
                        expendTxt.text = "Total Pengeluaran - Bensin Xenia"
                    }
                    else if(token == "3MNWbZwEdY") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Alat Mandi"
                    }else if(token == "l7DXScUPSK") {
                        expendTxt.text = "Total Pengeluaran - Jajan Anak-Anak"
                    }else if(token == "ZizaZCZz6W") {
                        expendTxt.text = "Total Pengeluaran - Makan Bunda Di Kantor"
                    }else if(token == "tZ2sAr9Jyx") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Sarapan dan Sayuran"
                    }else if(token == "GTcypTZSuP") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Cadangan Bunda"
                    }
                    val creditList = response.body()
                    if(creditList != null && creditList.daily != null) {

                        harianTxt.text = "${creditList.daily}"

                    }else{
                        harianTxt.text = "No Data"
                    }
                }else{
                    Log.e("retro", "Failed to get daily expend: ${response.code()}")
                    Toast.makeText(activity, "Failed to get daily expend", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DailyGet>, t: Throwable) {
                Log.e("Error", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun GetExpendMonthly(token: String){
        Server.setToken(token)
        api.getMontly().enqueue(object : Callback<MonthlyGet>{
            override fun onResponse(call: Call<MonthlyGet>, response: Response<MonthlyGet>) {
                if (response.isSuccessful){
                    if(token == "xujaTb51bh") {
                        expendTxt.text = "Total Pengeluaran - Bensin Xenia"
                    }
                    else if(token == "3MNWbZwEdY") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Alat Mandi"
                    }else if(token == "l7DXScUPSK") {
                        expendTxt.text = "Total Pengeluaran - Jajan Anak-Anak"
                    }else if(token == "ZizaZCZz6W") {
                        expendTxt.text = "Total Pengeluaran - Makan Bunda Di Kantor"
                    }else if(token == "tZ2sAr9Jyx") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Sarapan dan Sayuran"
                    }else if(token == "GTcypTZSuP") {
                        expendTxt.text = "Total Pengeluaran - Kebutuhan Cadangan Bunda"
                    }
                    val creditList = response.body()
                    if(creditList != null && creditList.monthly != null) {
                        monthlyTxt.text = "${creditList.monthly}"

                    }else{
                        monthlyTxt.text = "No Data"
                    }
                }else{
                    Log.e("retro", "Failed to get monthly expend: ${response.code()}")
                    Toast.makeText(activity, "Failed to get monthly expend", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MonthlyGet>, t: Throwable) {
                Log.e("Error", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun deleteData() {
        val requestBody = mapOf("method" to "POST", "action" to "delete_all")
        api.deleteAllData(requestBody).enqueue(object : Callback<DeleteAllResponse> {
            override fun onResponse(call: Call<DeleteAllResponse>, response: Response<DeleteAllResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.i("retro","Status: ${responseBody?.status}, Message: ${responseBody?.msg}")
                    Toast.makeText(activity, "All data succesfully deleted!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i("retro","Error: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Failed to delete data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteAllResponse>, t: Throwable) {
                Log.e("Error", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postCredit(value: String, msg: String, token: String, added_by: String) {

        api.postCredit(CreditRequest(value, msg, added_by)).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val bensinx = userTokens["bensin_x"].toString()
                    if(token == "xujaTb51bh") {
                        fetchCreditData(token)
                    }
                    else if(token == "3MNWbZwEdY") {
                        fetchCreditData2(token)
                    }else if(token == "l7DXScUPSK") {
                        fetchCreditData3(token)
                    }else if(token == "ZizaZCZz6W") {
                        fetchCreditData4(token)
                    }else if(token == "tZ2sAr9Jyx") {
                        fetchCreditData5(token)
                    }else if(token == "GTcypTZSuP") {
                        fetchCreditData6(token)
                    }
                    Log.i("retro", "Post credit: ${response.body()}")
                    Toast.makeText(activity, "Credit updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("retro", "Failed to post credit: ${response.code()}")
                    Toast.makeText(activity, "Failed to update credit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("Error", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postDebit(value: String, msg: String, token: String, added_by: String) {

        api.postDebit(DebitRequest(value, msg, added_by)).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val bensinx = userTokens["bensin_x"].toString()
                    if(token == "xujaTb51bh") {
                        fetchCreditData(token)
                    }
                    else if(token == "3MNWbZwEdY") {
                        fetchCreditData2(token)
                    }else if(token == "l7DXScUPSK") {
                        fetchCreditData3(token)
                    }else if(token == "ZizaZCZz6W") {
                        fetchCreditData4(token)
                    }else if(token == "tZ2sAr9Jyx") {
                        fetchCreditData5(token)
                    }else if(token == "GTcypTZSuP") {
                        fetchCreditData6(token)
                    }
                    Log.i("retro", "Post debit: ${response.body()}")
                    Toast.makeText(activity, "Debit processed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("retro", "Failed to post debit: ${response.code()}")
                    Toast.makeText(activity, "Failed to process debit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("Error", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun formatNumber(value: Int): String {
        return when {
            value >= 1_000_000_000 -> String.format("%.1fB", value / 1_000_000_000.0)
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0)
            value >= 1_000 -> String.format("%.1fK", value / 1_000.0)
            else -> value.toString()
        }
    }



    private fun fetchCreditData(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", token)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null && creditList.value != null)  {
                        //val latestCredit = creditList.last().value
//                        val formattedValue = formatNumber(creditList.value.toInt())
                            creditValueDisplay1.text = "Credit Value: Rp. ${creditList.value}"

                    } else {

                            creditValueDisplay1.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })




    }
    private fun fetchCreditData2(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", newToken)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null && creditList.value != null) {
                        //val latestCredit = creditList.last().value
//                        val formattedValue = formatNumber(creditList.value.toInt())
                            creditValueDisplay2.text = "Credit Value: Rp. ${creditList.value}"

                    } else {


                            creditValueDisplay2.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchCreditData3(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", newToken)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null  && creditList.value != null) {
                        //val latestCredit = creditList.last().value
                        //val formattedValue = formatNumber(creditList.value.toInt())
                        creditValueDisplay3.text = "Credit Value: Rp. ${creditList.value}"

                    } else {


                        creditValueDisplay3.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchCreditData4(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", newToken)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null && creditList.value != null) {
                        //val latestCredit = creditList.last().value
                        //val formattedValue = formatNumber(creditList.value.toInt())
                        creditValueDisplay4.text = "Credit Value: Rp. ${creditList.value}"

                    } else {


                        creditValueDisplay4.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchCreditData5(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", newToken)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null && creditList.value != null) {
                        //val latestCredit = creditList.last().value
                        //val formattedValue = formatNumber(creditList.value.toInt())
                        creditValueDisplay5.text = "Credit Value: Rp. ${creditList.value}"

                    } else {


                        creditValueDisplay5.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchCreditData6(newToken: String) {
        // Set the token dynamically
        pbData!!.visibility = View.VISIBLE
        Server.setToken(newToken)
        api.getCredit().enqueue(object : Callback<CreditResponse> {
            override fun onResponse(call: Call<CreditResponse>, response: Response<CreditResponse>) {
                val token = userTokens["bensin_x"].toString()
                Log.i("retro", newToken)
                if (response.isSuccessful) {
                    pbData!!.visibility = View.INVISIBLE
                    val creditList = response.body()
                    Log.i("retro", response.body().toString())
                    if (creditList != null && creditList.value != null) {
                        //val latestCredit = creditList.last().value
                        //val formattedValue = formatNumber(creditList.value.toInt())
                        creditValueDisplay6.text = "Credit Value: Rp. ${creditList.value}"

                    } else {


                        creditValueDisplay6.text = "No credit data available"

                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch credit data", Toast.LENGTH_SHORT).show()
                    Log.e("retro", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreditResponse>, t: Throwable) {
                Log.e("retro", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun startCountdown(deleteButton: Button) {
        // Create a CountDownTimer
        countdownTimer = object : CountDownTimer(countdownTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate the remaining time in seconds
                val secondsRemaining = millisUntilFinished / 1000

                // Determine the appropriate time unit (s, m, or h)
                val timeDisplay: String = when {
                    secondsRemaining < 60 -> {
                        "$secondsRemaining s" // Less than 1 minute, show seconds
                    }
                    secondsRemaining < 3600 -> {
                        val minutes = secondsRemaining / 60
                        "$minutes minute" // Less than 1 hour, show minutes
                    }
                    secondsRemaining < 86400 -> {
                        val hours = secondsRemaining / 3600
                        "$hours hour"  // Less than 1 day, show hours
                    }
                    else -> {
                        val minutes = secondsRemaining / 60
                        val hours = secondsRemaining / 3600
                        val days = secondsRemaining / 86400  // Calculate number of days
                        "$days day"  // More than 1 day, show days
                    }
                }

                // Update the button text with the formatted countdown
                deleteButton.text = "Delete All Data ($timeDisplay)"
            }

            override fun onFinish() {
                // When the countdown finishes, update the button text
                if(email == "azka.jsiswanto@gmail.com") {
                    deleteButton.text = "Delete All Data"
                    deleteButton.isEnabled = true  // Disable the button when time is up
                }else{
                    deleteButton.text = "Delete All Data"
                    deleteButton.isEnabled = false  // Disable the button when time is up
                }
            }
        }

        // Start the countdown
        countdownTimer.start()

        // Enable the button at the start
        deleteButton.isEnabled = false
    }


    private fun showConfirmationDialog() {
        // Build the AlertDialog
        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to delete all data?")
            .setPositiveButton("Yes") { _, _ ->
                // Proceed with the action (e.g., delete the data)
                deleteData()
                val bensinx = userTokens["bensin_x"].toString()
                val sabun = userTokens["sabun"].toString()
                val jajan = userTokens["jajan"].toString()
                val makan = userTokens["makan"].toString()
                val ss = userTokens["ss"].toString()
                val cadangan = userTokens["cadangan"].toString()
                fetchCreditData(bensinx)
                fetchCreditData2(sabun)
                fetchCreditData3(jajan)
                fetchCreditData4(makan)
                fetchCreditData5(ss)
                fetchCreditData6(cadangan)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the countdown timer when the view is destroyed
        countdownTimer.cancel()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinansialFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinansialFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
