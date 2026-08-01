package com.indodevstudio.azka_home_iot

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

class ShortcutsManager(private val context: Context) {

    companion object {
        const val SHORTCUT_ONE_ID = "shortcut_one_id"
        const val SHORTCUT_TWO_ID = "shortcut_two_id"
        const val SHORTCUT_THREE_ID = "shortcut_three_id"
    }

    fun createShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val listOfShortcuts = getShortcutsList()
            ShortcutManagerCompat.setDynamicShortcuts(context, listOfShortcuts)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun getShortcutsList(): List<ShortcutInfoCompat> {
        val shortcutOne = buildShortcut(
            id = SHORTCUT_ONE_ID,
            shortLabel = context.getString(R.string.shortcut_one_short_label),
            longLabel = context.getString(R.string.shortcut_one_long_label),
            intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("fragment_to_open", "finansial")
            },
            shortcutIcon = R.drawable.ic_financial_new_blue
        )

        val shortcutTwo = buildShortcut(
            id = SHORTCUT_TWO_ID,
            shortLabel = context.getString(R.string.shortcut_two_short_label),
            longLabel = context.getString(R.string.shortcut_two_long_label),
            intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("fragment_to_open", "event")
            },
            shortcutIcon = R.drawable.ic_event_blue
        )

        val shortcutThree = buildShortcut(
            id = SHORTCUT_THREE_ID,
            shortLabel = context.getString(R.string.shortcut_three_short_label),
            longLabel = context.getString(R.string.shortcut_three_long_label),
            intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("fragment_to_open", "arduino")
            },
            shortcutIcon = R.drawable.ic_arduino_blue
        )

        return listOf(shortcutOne, shortcutTwo, shortcutThree)
    }


    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun buildShortcut(
        id: String,
        shortLabel: String,
        longLabel: String,
        intent: Intent,
        shortcutIcon: Int
    ): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIntent(intent)
            .setIcon(IconCompat.createWithResource(context, shortcutIcon))
            .build()
    }
}
