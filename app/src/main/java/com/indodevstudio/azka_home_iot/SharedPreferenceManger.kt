package com.indodevstudio.azka_home_iot

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate

class SharedPreferenceManger(context: Context) {
    private val preference = context.getSharedPreferences(
        context.packageName,
        MODE_PRIVATE
    )
    private val editor = preference.edit()

    private val keyTheme = "dark_mode"

    var theme
        get() = preference.getInt(keyTheme, 0)
        set(value) {
            editor.putInt(keyTheme, value)
            editor.commit()
        }

    val themeFlag = arrayOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES

    )
}