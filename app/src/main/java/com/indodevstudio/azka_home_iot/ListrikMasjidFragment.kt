package com.indodevstudio.azka_home_iot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TamanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ListrikMasjidFragment : Fragment(), View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var button : Button
    lateinit var myWebView : WebView
    lateinit var Statustxt : TextView

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
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_listrik_masjid, container, false)
        button = view.findViewById(R.id.status_btn)
        myWebView = view.findViewById(R.id.myWeb)
        Statustxt = view.findViewById(R.id.statusText)
        button.setOnClickListener(this)
        myWebView.getSettings().setJavaScriptEnabled(true);
        //myWebView.setWebViewClient(WebViewClient())
        myWebView.setWebViewClient(object : WebViewClient() {
            //override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            //    view.loadUrl(url)
            //    return true
            //}
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError
            ) {

                super.onReceivedError(view, request, error)
                Log.e("MainActivity", (error.description as String))
                Log.e("MainActivity", error.errorCode.toString())
                Log.d("ERROR","Error code:"+Integer.toString(error.errorCode))
                println(error.errorCode)

                Toast.makeText(getActivity(),
                    "Your Internet Connection May not be active Or " + error.description,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        return view
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.status_btn-> {
                println("STATUS!")
                val URL : String ="https://abeazka.my.id/telemetri/input-wa.php?type=listrik_masjid&value1=listrik"

                //myWebView.loadUrl("http://taryem.my.id/Lab01/labx.php?type=on")
                //myWebView.loadUrl("http://taryem.my.id/Lab01/labx.php?type=on")
                if (URL.isNotEmpty()){
                    val http = OkHttpClient()
                    val request = Request.Builder()
                        .url(URL)
                        .build()
                    //myWebView.loadUrl(URL)

                    http.newCall(request).enqueue(object : Callback{
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
                            Log.i("KODE", "CODE: "+ responseCode)
                            Log.i("Response", "Received response from server. Response")
                            if (response.code == 200){

                                getActivity()?.runOnUiThread{
                                    Statustxt.text = results
                                    Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                getActivity()?.runOnUiThread {
                                    Statustxt.text = "ERROR!"
                                    Log.e(
                                        "HTTP Error",
                                        "Something didn't load, or wasn't succesfully"
                                    )
                                    Toast.makeText(getActivity(), "Fail", Toast.LENGTH_LONG).show();

                                }
                                return
                            }
                        }
                    })
                }


            }
        }
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TamanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListrikMasjidFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}