package com.protone.common.context

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object MApplication {

    val app: Application get() = application

    var screenHeight: Int = 0
    var screenWidth: Int = 0

    private lateinit var application: Application

    fun init(application: Application) {
        MApplication.application = application
    }

    fun startActivity(reference: String, triggerAtMillis: Long) {
        val clazz = Class.forName(reference) ?: return
        Intent(application, clazz).let {
            if (activities.isNotEmpty()) {
                application.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            }
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
            val restartIntent = PendingIntent.getActivity(application, 0, it, flag)
            (application.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.set(
                AlarmManager.RTC,
                triggerAtMillis,
                restartIntent
            )
        }
    }
}