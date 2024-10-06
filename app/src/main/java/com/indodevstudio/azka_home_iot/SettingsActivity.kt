package com.indodevstudio.azka_home_iot

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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
    }

    override fun onDestroy() {

        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}