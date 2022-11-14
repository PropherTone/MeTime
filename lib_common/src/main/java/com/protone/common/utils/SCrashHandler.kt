package com.protone.common.utils

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

object SCrashHandler : Thread.UncaughtExceptionHandler {

    var path: String? = null

    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null) {
            writeLog(e, t)
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        } else {
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }

    fun writeLog(e: Throwable, t: Thread? = null) {
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        e.printStackTrace(printWriter)
        printWriter.close()
        val cause = writer.toString()
        val crashFile = File(path ?: "".apply {
            if (t != null) defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            return
        })
        if (crashFile.exists()) crashFile.delete()
        crashFile.createNewFile()
        val fileWriter = FileWriter(crashFile)
        fileWriter.write(cause)
        fileWriter.close()
    }
}