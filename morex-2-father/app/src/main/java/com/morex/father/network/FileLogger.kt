package com.morex.father.network

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val FILE_NAME = "morex_log.txt"
    private var logFile: File? = null

    fun init(context: Context) {
        try {
            // تخزين داخلي خاص بالتطبيق (لا يقرؤه تطبيق آخر)، ومحدود الحجم
            logFile = File(context.filesDir, FILE_NAME)
            if ((logFile?.length() ?: 0L) > 512 * 1024) logFile?.delete()
            write("════════ LOG STARTED ════════")
        } catch (e: Exception) {
            logFile = null
        }
    }

    fun write(msg: String) {
        try {
            val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            logFile?.appendText("[$ts] $msg\n")
        } catch (e: Exception) { }
    }

    fun path(): String? = logFile?.absolutePath
}
