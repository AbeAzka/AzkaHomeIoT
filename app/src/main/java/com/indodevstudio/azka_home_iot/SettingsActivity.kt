package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.datatransport.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        web = findViewById(R.id.web)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Handle Clear Cache
            val clearCachePref = findPreference<Preference>("clear_cache")
            clearCachePref?.setOnPreferenceClickListener {
                context?.cacheDir?.deleteRecursively()
                Toast.makeText(context, "Cache berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                true
            }

            // TAMBAHAN: Handle Contact Support
            findPreference<Preference>("contact_support")?.setOnPreferenceClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:hi@indodevstudio.my.id")
                    putExtra(Intent.EXTRA_SUBJECT, "Support: Azka Home IoT")
                }
                startActivity(Intent.createChooser(emailIntent, "Kirim Email via..."))
                true
            }

            // Handle App Version Dynamic Text
            val appVersionPref = findPreference<Preference>("app_version")
            appVersionPref?.summary = "Versi " + BuildConfig.VERSION_NAME
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "dark_mode" -> {
                val prefs = sharedPreferences.getString(key, "0")
                val sharedPreferenceManger = SharedPreferenceManger(this)
                val checkedTheme = prefs?.toInt() ?: sharedPreferenceManger.theme

                sharedPreferenceManger.theme = checkedTheme
                AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])
            }

            "notification_toggle" -> {
                val isEnabled = sharedPreferences.getBoolean(key, true)
                if (isEnabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic("general_notifications")
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("general_notifications")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}