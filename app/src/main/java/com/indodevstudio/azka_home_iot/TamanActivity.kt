package com.indodevstudio.azka_home_iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TamanActivity : AppCompatActivity(), View.OnClickListener {
    //lateinit var toolbar: Toolbar
    lateinit var button : Button
    lateinit var button2 : Button
    lateinit var myWebView : WebView
    lateinit var texto : TextView
    var statusCode = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taman)
        button = findViewById(R.id.button)
        button2 = findViewById(R.id.button2)
        myWebView = findViewById(R.id.myweb)
        //toolbar = findViewById(R.id.myToolBar)
        //toolbar.title = "Abe Azka Iot"
        //setSupportActionBar(toolbar)
        texto = findViewById(R.id.textView3)
        button.setOnClickListener(this)
        button2.setOnClickListener(this)
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
                Log.e("MainActivity", (error.description as String)!!)
                Log.e("MainActivity", error.errorCode.toString())
                Log.d("ERROR","Error code:"+Integer.toString(error.errorCode))
                println(error.errorCode)

                Toast.makeText(applicationContext,
                    "Your Internet Connection May not be active Or " + error.description,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.button-> {
                println("On!")
                val URL : String ="http://taryem.my.id/Lab01/labx.php?type=on"

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
                            Log.i("KODE", "CODE: "+ responseCode)
                            Log.i("Response", "Received response from server. Response")
                            if (response.code == 200){
                                runOnUiThread{
                                    texto.text = "Turn On Light"
                                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Log.e("HTTP Error", "Something didn't load, or wasn't succesfully")
                                Toast.makeText(applicationContext, "Fail", Toast.LENGTH_LONG).show();
                                return
                            }
                        }
                    })
                }


            }
            R.id.button2-> {
                println("Off!")
                //myWebView.loadUrl("http://taryem.my.id/Lab01/labx.php?type=off")
                val URL : String ="http://taryem.my.id/Lab01/labx.php?type=off"

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
                            Log.i("KODE", "CODE: "+ responseCode)
                            Log.i("Response", "Received response from server. Response")
                            if (response.code == 200){
                                runOnUiThread{
                                    texto.text = "Turn Off Lights"
                                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Log.e("HTTP Error", "Something didn't load, or wasn't succesfully")

                                Toast.makeText(applicationContext, "Fail", Toast.LENGTH_LONG).show();
                                return
                            }
                        }
                    })
                }
            }
        }
    }

}