package com.indodevstudio.azka_home_iot

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var btnFullscreen: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnReload: ImageButton
    private lateinit var webTitle: TextView
    private var isFullscreen = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        webView = view.findViewById(R.id.webview)
        btnFullscreen = view.findViewById(R.id.btnFullscreen)
        btnBack = view.findViewById(R.id.btnBack)
        btnForward = view.findViewById(R.id.btnForward)
        webTitle = view.findViewById(R.id.webTitle)
        btnReload = view.findViewById(R.id.btnReload)
        /*toolbarOverlay = view.findViewById(R.id.toolbarOverlay)*/

        // Set up WebView
        setupWebView()

        // Tombol navigasi
        btnBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        btnForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        // Tombol fullscreen
        btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        btnReload.setOnClickListener {
            webView.reload()

            Toast.makeText(requireContext(), "Halaman di-reload", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                updateNavigationButtons()
                webTitle.text = webView.title ?: "Tanpa Judul"
            }
        }

        enableDarkModeIfSupported(webView)

        webView.loadUrl("https://www.indodevstudio.my.id/cbt")
    }

    private fun updateNavigationButtons() {
        btnBack.isEnabled = webView.canGoBack()
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f

        btnForward.isEnabled = webView.canGoForward()
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }


    private fun toggleFullscreen() {
        val activity = requireActivity()
        val window = activity.window

        if (!isFullscreen) {
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )

            /*toolbarOverlay.animate().alpha(0f).withEndAction {
                toolbarOverlay.visibility = View.GONE
            }.start()*/
            btnFullscreen.tooltipText = "Keluar fullscreen"
            btnFullscreen.setImageResource(R.drawable.ic_exit_fullscreen_white)
            isFullscreen = true

        } else {
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.show()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

            /*toolbarOverlay.visibility = View.VISIBLE
            toolbarOverlay.alpha = 0f
            toolbarOverlay.animate().alpha(1f).start()*/
            btnFullscreen.tooltipText = "Masuk fullscreen"
            btnFullscreen.setImageResource(R.drawable.ic_enter_fullscreen_white)
            isFullscreen = false
        }
    }


    private fun enableDarkModeIfSupported(webView: WebView) {
        val isDarkTheme = (webView.context.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(
                webView.settings,
                if (isDarkTheme)
                    WebSettingsCompat.FORCE_DARK_ON
                else
                    WebSettingsCompat.FORCE_DARK_OFF
            )
        }
    }

    fun canGoBack(): Boolean = webView.canGoBack()
    fun goBack() = webView.goBack()
}
