package com.indodevstudio.azka_home_iot

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessaging


class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
lateinit var web : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        web = findViewById(R.id.web)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Settings"



        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == "dark_mode"){
            val prefs = sharedPreferences.getString(key, "0")
            val sharedPreferenceManger = SharedPreferenceManger(this)
            var checkedTheme = sharedPreferenceManger.theme
            if (prefs != null) {
                checkedTheme = prefs.toInt()
            }
            when(prefs?.toInt()){

                0 ->{
                    //AppCompatDelegate.setDefaultNightMode(
//                   AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
//                    )
//                    checkedTheme = 1
                    sharedPreferenceManger.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])
                }
                1 ->{
                    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                    delegate.applyDayNight()
//                    checkedTheme = 2
                    sharedPreferenceManger.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])
                }
                2 ->{
                    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                    delegate.applyDayNight()
//                    checkedTheme = 3
                    sharedPreferenceManger.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])
                }
            }
        }

        /*if (key == "notification") {
            val prefs = sharedPreferences.getBoolean(key, true)

            if (prefs) {
                // Enable notifications
                // For example, using the NotificationManager to show a notification

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationChannelId = "event_reminder" // You can create your channel

                // Create and show the notification
                val notification = NotificationCompat.Builder(this, notificationChannelId)
                    .setSmallIcon(R.drawable.ic_notif_ig)
                    .setContentTitle("Notification Enabled")
                    .setContentText("Notifications are turned on.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                // Show the notification
                notificationManager.notify(0, notification)

                // Enable Firebase notifications (Subscribe to topic)
                FirebaseMessaging.getInstance().subscribeToTopic("general_notifications")
                    .addOnCompleteListener { task ->
                        var msg = "Subscribed to notifications"
                        if (!task.isSuccessful) {
                            msg = "Failed to subscribe"
                        }
                        Log.d("Notification", msg)
                    }

            } else {
                // Disable notifications
                // Cancel ongoing notifications if any
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()  // This will cancel all active notifications

                FirebaseMessaging.getInstance().unsubscribeFromTopic("general_notifications")
                    .addOnCompleteListener { task ->
                        var msg = "Unsubscribed from notifications"
                        if (!task.isSuccessful) {
                            msg = "Failed to unsubscribe"
                        }
                        Log.d("Notification", msg)
                    }
            }
        }*/


    }

    override fun onDestroy() {

        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}