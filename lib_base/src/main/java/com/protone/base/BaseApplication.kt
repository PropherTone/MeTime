package com.protone.base

import android.app.Application
import android.content.Context
import com.protone.common.baseType.DPI
import com.protone.common.context.MApplication
import com.protone.common.utils.SCrashHandler
import com.protone.common.utils.renderUtils.Blur
import com.protone.common.utils.todayDate
import java.io.File

class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Blur.init(this)
        MApplication.init(this)
        DPI = MApplication.app.resources.displayMetrics.densityDpi
        val file = File("${base?.externalCacheDir?.path}/CrashLog")
        val result = if (!file.exists()) {
            file.mkdirs()
        } else true
        val todayDate = todayDate("yyyy_MM_dd_HH_mm_ss")
        SCrashHandler.path =
            if (result) "${base?.externalCacheDir?.path}/CrashLog/s_crash_log_${todayDate}.txt"
            else "${base?.externalCacheDir?.path}/s_crash_log_${todayDate}.txt"
    }

}