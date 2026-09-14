package com.indodevstudio.azka_home_iot

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val versionText = view.findViewById<TextView>(R.id.version)

        try {
            // Cara yang lebih aman untuk mengambil versionName
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            versionText.text = "Version: ${pInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            versionText.text = "Version: Unknown"
        }

        return view
    }
}