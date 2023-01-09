package com.protone.common.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.protone.common.context.MApplication
import com.protone.common.context.activities
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

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null) {
            writeLog(e, t)
            MApplication.startActivity(
                "com.protone.metime.activity.SplashActivity",
                System.currentTimeMillis() + 1000L
            )
            val iterator: MutableIterator<Activity> = activities.iterator()
            while (iterator.hasNext()) {
                iterator.next().finish()
                iterator.remove()
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        } else {
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }
}