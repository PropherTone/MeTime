package com.protone.common.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.protone.common.baseType.getParentPath
import com.protone.common.context.MApplication
import com.protone.common.context.activities
import java.io.*
import kotlin.system.exitProcess


object SCrashHandler : Thread.UncaughtExceptionHandler {

    var path: String? = null

    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun writeLog(logPath: String, e: Throwable, t: Thread? = null) {
        if (logPath.isEmpty()) return
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        printWriter.println("Thread:${t?.name}")
        e.printStackTrace(printWriter)
        printWriter.close()
        val cause = writer.toString()
        val dir = File(logPath.getParentPath())
        if (dir.isDirectory) dir.listFiles()
            ?.takeIf { it.size >= 12 }
            ?.let { dir.deleteRecursively() }
        val crashFile = File(logPath)
        if (crashFile.exists()) crashFile.delete()
        crashFile.createNewFile()
        val fileWriter = FileWriter(crashFile)
        fileWriter.write(cause)
        fileWriter.close()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null) {
            try {
                writeLog(path ?: "", e, t)
            } catch (e: IOException) {
                e.printStackTrace()
            }
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