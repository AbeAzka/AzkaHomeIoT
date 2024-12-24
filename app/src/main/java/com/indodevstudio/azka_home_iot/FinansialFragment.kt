package com.indodevstudio.azka_home_iot

import ApiService
import CreditRequest
import CreditResponse
import DebitRequest
import DeleteAllResponse
import Server
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private lateinit var spinner: Spinner
    private val api = Server.instance.create(ApiService::class.java)
    val userTokens = mapOf(
        "bensin_x" to "xujaTb51bh",
        "sabun" to "3MNWbZwEdY",
        "jajan" to "l7DXScUPSK"
    )

    var bensin = false
    var kebutuhan = false
    var srlData: SwipeRefreshLayout? = null
    var pbData: ProgressBar? = null
    private lateinit var countdownTimer: CountDownTimer
    private val countdownTimeInMillis: Long = 10000      // 1 hour and 1 minute (for testing purposes)
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



        srlData = view.findViewById(R.id.srl_data)
        pbData = view.findViewById(R.id.pb_data)

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

                            fetchCreditData(bensinx)



                        cardView1.visibility = View.VISIBLE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.GONE
                        Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Kebutuhan_Alat_Mandi" -> {
                        // Action for "Bensin"
                        val sabun = userTokens["sabun"].toString()
                        fetchCreditData2(sabun)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.VISIBLE
                        cardView3.visibility = View.GONE
                        Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    "Jajan_Anak" -> {
                        // Action for "Bensin"
                        val jajan = userTokens["jajan"].toString()
                        fetchCreditData3(jajan)
                        cardView1.visibility = View.GONE
                        cardView2.visibility = View.GONE
                        cardView3.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "You selected $selectedItem", Toast.LENGTH_SHORT).show()
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
                fetchCreditData(bensinx)
                fetchCreditData2(sabun)
                fetchCreditData3(jajan)

                setRefreshing(false)

            }
        }
        startCountdown(deleteAllData)
// Set up button click listeners
        deleteAllData.setOnClickListener{

            showConfirmationDialog()
        }
        submitCredit1.setOnClickListener {
            val creditValue = valueInput1.text.toString()
            val msg = descInput1.text.toString()
            if (creditValue != null) {
                val bensinx = userTokens["bensin_x"].toString()
                postCredit(creditValue,msg, bensinx)
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
                postDebit(debitValue, msg, bensinx)
                valueInput2.text.clear()
                descInput2.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit2.setOnClickListener {
            val creditValue = valueInput2.text.toString()
            val msg = descInput2.text.toString()
            if (creditValue != null) {
                val sabun = userTokens["sabun"].toString()
                postCredit(creditValue,msg, sabun)
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
                postDebit(debitValue, msg, sabun)
                valueInput1.text.clear()
                descInput1.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }

        submitCredit3.setOnClickListener {
            val creditValue = valueInput3.text.toString()
            val msg = descInput3.text.toString()
            if (creditValue != null) {
                val jajan = userTokens["jajan"].toString()
                postCredit(creditValue,msg, jajan)
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
                postDebit(debitValue, msg, jajan)
                valueInput3.text.clear()
                descInput3.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a valid value", Toast.LENGTH_SHORT).show()
            }
        }




        return view
    }

    private fun sendBoolToServer(value: Boolean) {
        // Use the boolean value as needed
        if (value) {
            // Handle "Yes" or true
        } else {
            // Handle "No" or false
        }
    }

    private fun deleteData() {
        val requestBody = mapOf("method" to "POST", "action" to "delete_all")
        api.deleteAllData(requestBody).enqueue(object : Callback<DeleteAllResponse> {
            override fun onResponse(call: Call<DeleteAllResponse>, response: Response<DeleteAllResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.i("retro","Status: ${responseBody?.status}, Message: ${responseBody?.msg}")
                    Toast.makeText(activity, "All data succesfully deleted!", Toast.LENGTH_SHORT).show()
                } else {4
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

    private fun postCredit(value: String, msg: String, token: String) {

        api.postCredit(CreditRequest(value, msg)).enqueue(object : Callback<Map<String, String>> {
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

    private fun postDebit(value: String, msg: String, token: String) {

        api.postDebit(DebitRequest(value, msg)).enqueue(object : Callback<Map<String, String>> {
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
                    if (creditList != null ) {
                        //val latestCredit = creditList.last().value

                            creditValueDisplay1.text = "Credit Value: ${creditList.value}"

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
                    if (creditList != null ) {
                        //val latestCredit = creditList.last().value

                            creditValueDisplay2.text = "Credit Value: ${creditList.value}"

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
                    if (creditList != null ) {
                        //val latestCredit = creditList.last().value

                        creditValueDisplay3.text = "Credit Value: ${creditList.value}"

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
                deleteButton.text = "Delete Data ($timeDisplay)"
            }

            override fun onFinish() {
                // When the countdown finishes, update the button text
                deleteButton.text = "Delete Data"
                deleteButton.isEnabled = true  // Disable the button when time is up
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

        val dialogStyle = if (isNightMode) R.style.CustomAlertDialogStyle_Night else R.style.CustomAlertDialogStyle
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Data")
            .setMessage("Are you sure you want to delete all data?")
            .setPositiveButton("Yes") { dialog, which ->
                // Proceed with the action (e.g., delete the data)
                deleteData()
            }
            .setNegativeButton("No") { dialog, which ->
                // Dismiss the dialog, no action needed
                dialog.dismiss()
            }
            .create()

        // Show the dialog
        dialog.show()
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