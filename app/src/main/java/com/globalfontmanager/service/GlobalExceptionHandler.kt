package com.globalfontmanager.service

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class GlobalExceptionHandler(
    context: Context,
    private val delegate: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler(),
) : Thread.UncaughtExceptionHandler {
    private val crashLog = File(context.applicationContext.filesDir, "crash.log")

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            crashLog.parentFile?.mkdirs()
            val trace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
            crashLog.appendText("\n--- ${System.currentTimeMillis()} ${thread.name} ---\n$trace")
        }
        delegate?.uncaughtException(thread, throwable)
    }
}
