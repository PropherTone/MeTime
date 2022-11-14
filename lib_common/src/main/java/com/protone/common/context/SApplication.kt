package com.protone.common.context

import android.app.Application

object SApplication {

    val app: Application get() = application

    var screenHeight: Int = 0
    var screenWidth: Int = 0

    private lateinit var application: Application

    fun init(application: Application) {
        SApplication.application = application
    }
}