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

    private var intent: Intent? = null

    fun setIntent(intent: Intent) {
        this.intent = intent
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null) {
            writeLog(e, t)
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            intent?.let {
                it.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_NEW_TASK
                )
                val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                }
                val restartIntent = PendingIntent.getActivity(MApplication.app, 0, intent, flag)
                (MApplication.app.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.set(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + 1000L,
                    restartIntent
                )
            }
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