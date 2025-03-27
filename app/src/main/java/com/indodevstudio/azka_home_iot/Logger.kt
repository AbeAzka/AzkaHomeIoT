package com.indodevstudio.azka_home_iot

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.getExternalFilesDir(null), "logcat_logs.txt")

        // Tangkap crash (uncaught exceptions)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log("CRASH", "Thread: ${thread.name} | Error: ${throwable.localizedMessage}")
            log("CRASH", Log.getStackTraceString(throwable)) // Simpan stack trace

            // Default handler tetap berjalan agar sistem bisa menangani crash
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }

    fun log(tag: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp [$tag]: $message"

        // Print ke Logcat juga
        android.util.Log.d(tag, message)

        // Simpan ke file
        logFile?.let {
            FileWriter(it, true).use { writer ->
                writer.append("$logMessage\n")
                writer.flush()
            }
        }
    }
}
