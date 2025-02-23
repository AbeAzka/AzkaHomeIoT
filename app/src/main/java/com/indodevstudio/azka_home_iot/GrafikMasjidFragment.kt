package com.indodevstudio.azka_home_iot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import com.ortiz.touchview.TouchImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TamanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class GrafikMasjidFragment : Fragment(), View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var button : Button
    lateinit var myWebView : WebView
    lateinit var imageGrafik : TouchImageView
    lateinit var inputStream : InputStream
    var image : Bitmap? = null
    var click = false

    //lateinit var Statustxt : TextView

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
        val view =  inflater.inflate(R.layout.fragment_grafik_masjid, container, false)
        val view2 =  inflater.inflate(R.layout.popup_grafik, container, false)
        button = view.findViewById(R.id.status_btn)
        myWebView = view.findViewById(R.id.myWeb)
        imageGrafik =view2.findViewById(R.id.imageGrafikPop)
        //Statustxt = view.findViewById(R.id.statusText)
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
        when (view?.id) {
            R.id.status_btn -> {
                if (click == true){
                    getActivity()?.runOnUiThread {
                        Toast.makeText(
                            getActivity(),
                            "You already click this button!",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }else {
                    click = true
                    println("STATUS!")
                    val URL: String = "https://abeazka.my.id/telemetri/tandongrafik.php"
                    if (URL.isNotEmpty()) {
                        val http = OkHttpClient()
                        val request = Request.Builder()
                            .url(URL)
                            .build()
                        //myWebView.loadUrl(URL)

                        http.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace();
                                click = false
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
                                    val URL2 =
                                        URL("https://abeazka.my.id/telemetri/tandon/grafiktandon_masjid-x.php.png")
                                    try {
                                        val `in` = URL2.openStream()
                                        image = BitmapFactory.decodeStream(`in`)
                                        handler.post {
                                            //imageGrafik.setImageBitmap(image)

                                            var inflater = LayoutInflater.from(getActivity())
                                            var popupview =
                                                inflater.inflate(R.layout.popup_grafik, null, false)
                                            var imagee =
                                                popupview.findViewById<ImageView>(R.id.imageGrafikPop)
                                            var close =
                                                popupview.findViewById<ImageView>(R.id.close)
                                            var builder = PopupWindow(
                                                popupview,
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                true
                                            )
                                            imagee.setImageBitmap(image)
                                            //imagee.setRotation(90f)
                                            builder.setBackgroundDrawable(
                                                getDrawable(
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


                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }



                                    getActivity()?.runOnUiThread {
                                        Toast.makeText(
                                            getActivity(),
                                            "Success",
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    }
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
                //Thread.sleep(1_000)

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
            GrafikMasjidFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}