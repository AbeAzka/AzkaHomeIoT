package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class RingtonePickerFragment : Fragment() {

    private val RINGTONE_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pickRingtone()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun pickRingtone() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Pilih Nada Dering")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)

            val currentRingtoneUri = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getString("notification_ringtone", null)

            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                if (currentRingtoneUri != null) Uri.parse(currentRingtoneUri) else null
            )
        }

        startActivityForResult(intent, RINGTONE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            // Simpan ke SharedPreferences
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putString("notification_ringtone", uri?.toString() ?: "")
                .apply()

            Toast.makeText(requireContext(), "Nada dering disimpan", Toast.LENGTH_SHORT).show()
        }

        // Kembali ke Settings
        parentFragmentManager.popBackStack()
    }
}
