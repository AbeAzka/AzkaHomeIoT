package com.indodevstudio.azka_home_iot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

import androidx.activity.enableEdgeToEdge

import java.net.InetAddress
import java.net.NetworkInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SholatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SholatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var webView: WebView
    private lateinit var webView2: WebView

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
        val view =  inflater.inflate(R.layout.fragment_sholat, container, false)
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val obfuscatedEmail = email?.let { obfuscateEmail(it) }
        val peh = obfuscatedEmail.toString()
        Log.i("P", obfuscatedEmail.toString());

        webView = view.findViewById(R.id.web_view)
        webView2 = view.findViewById(R.id.web_view2)

        // Configure WebView
        webView.webViewClient = WebViewClient() // Prevents opening in the browser
        webView.settings.javaScriptEnabled = true // Enable JavaScript if needed

        webView2.webViewClient = WebViewClient() // Prevents opening in the browser
        webView2.settings.javaScriptEnabled = true // Enable JavaScript if needed



        val executeUrlButton3: Button = view.findViewById(R.id.executeUrlButton3)

        executeUrlButton3.setOnClickListener {
            // Execute the URL request in the background
            val url3 = "https://abeazka.my.id/$peh/inputperf.php?type=$peh&value1=1" // Replace with your desired URL
            Log.i("P", url3)
            //webView.loadUrl(url)
            webView2.webViewClient = object : WebViewClient() {

                // This method is triggered on HTTP errors (non-200 responses)
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val statusCode = errorResponse?.statusCode
                    if (statusCode != null && statusCode != 200) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.GONE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                    if (statusCode != null && statusCode == 404) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.VISIBLE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                }

                // This method is triggered for network-level errors (e.g., no internet connection)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (error != null && error.description.toString() != "net::ERR_CLEARTEXT_NOT_PERMITTED") {
                        webView2.visibility = WebView.GONE
                        Toast.makeText(
                            getActivity(),
                            "Network Error: ${error?.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            webView2.loadUrl(url3)
        }

        val executeUrlButton4: Button = view.findViewById(R.id.executeUrlButton4)

        executeUrlButton4.setOnClickListener {
            // Execute the URL request in the background
            val url4 = "https://abeazka.my.id/$peh/inputperf.php?type=$peh&value1=0.5" // Replace with your desired URL
            //webView.loadUrl(url)
            webView2.webViewClient = object : WebViewClient() {

                // This method is triggered on HTTP errors (non-200 responses)
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val statusCode = errorResponse?.statusCode
                    if (statusCode != null && statusCode != 200) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.GONE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                    if (statusCode != null && statusCode == 404) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.VISIBLE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                }

                // This method is triggered for network-level errors (e.g., no internet connection)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (error != null && error.description.toString() != "net::ERR_CLEARTEXT_NOT_PERMITTED") {
                        webView2.visibility = WebView.GONE
                        Toast.makeText(
                            getActivity(),
                            "Network Error: ${error?.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
            webView2.loadUrl(url4)
        }

        val executeUrlButton5: Button = view.findViewById(R.id.executeUrlButton5)

        executeUrlButton5.setOnClickListener {
            // Execute the URL request in the background
            val url5 = "https://abeazka.my.id/$peh/inputperf.php?type=$peh&value1=-0.5" // Replace with your desired URL
            //webView.loadUrl(url)
            webView2.webViewClient = object : WebViewClient() {

                // This method is triggered on HTTP errors (non-200 responses)
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val statusCode = errorResponse?.statusCode
                    if (statusCode != null && statusCode != 200) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.GONE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                    if (statusCode != null && statusCode == 404) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.VISIBLE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                }

                // This method is triggered for network-level errors (e.g., no internet connection)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (error != null && error.description.toString() != "net::ERR_CLEARTEXT_NOT_PERMITTED") {
                        webView2.visibility = WebView.GONE
                        Toast.makeText(
                            getActivity(),
                            "Network Error: ${error?.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
            webView2.loadUrl(url5)
        }

        val executeUrlButton6: Button = view.findViewById(R.id.executeUrlButton6)

        executeUrlButton6.setOnClickListener {
            // Execute the URL request in the background
            val url6 = "https://abeazka.my.id/$peh/inputperf.php?type=$peh&value1=-1" // Replace with your desired URL
            //webView.loadUrl(url)
            webView2.webViewClient = object : WebViewClient() {

                // This method is triggered on HTTP errors (non-200 responses)
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val statusCode = errorResponse?.statusCode
                    if (statusCode != null && statusCode != 200) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.GONE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                    if (statusCode != null && statusCode == 404) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView2.visibility = WebView.VISIBLE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                }

                // This method is triggered for network-level errors (e.g., no internet connection)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (error != null && error.description.toString() != "net::ERR_CLEARTEXT_NOT_PERMITTED") {
                        webView2.visibility = WebView.GONE
                        Toast.makeText(
                            getActivity(),
                            "Network Error: ${error?.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
            webView2.loadUrl(url6)
        }

        val executeUrlButton7: Button = view.findViewById(R.id.executeUrlButton7)

        executeUrlButton7.setOnClickListener {
            webView.clearCache(true)
            /*webView.clearHistory()*/
            // Execute the URL request in the background
            val url71 = "https://abeazka.my.id/$peh/perfgrafik.php"
            webView2.loadUrl(url71)
            val url7 = "https://abeazka.my.id/$peh/grafik/grafikperf.php.png" // Replace with your desired URL
            //webView.loadUrl(url)

            webView.webViewClient = object : WebViewClient() {

                // This method is triggered on HTTP errors (non-200 responses)
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    val statusCode = errorResponse?.statusCode
                    if (statusCode != null && statusCode != 200) {
                        // If status code is not 200 (OK), show the error UI and set up retry

                        webView.visibility = WebView.GONE
                        Toast.makeText(getActivity(), "HTTP Error: $statusCode", Toast.LENGTH_LONG).show()


                    }
                }

                // This method is triggered for network-level errors (e.g., no internet connection)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (error != null && error.description.toString() != "net::ERR_CLEARTEXT_NOT_PERMITTED") {
                        webView.visibility = WebView.GONE
                        Toast.makeText(
                            getActivity(),
                            "Network Error: ${error?.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            val htmlContent = """
            <html>
                <body>
                    <img src="$url7" />
                </body>
            </html>
        """
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
        return view
    }

    fun obfuscateEmail(email: String): String {
        val parts = if (email.contains(".")) {
            email.split("@")[0].split(".")
        } else {
            listOf(email.split("@")[0])
        }
        val username = parts.first()
        return "$username"
        //azka.jsiswanto@gmail.com
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SholatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SholatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}