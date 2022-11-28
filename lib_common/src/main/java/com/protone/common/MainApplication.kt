package com.protone.common

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.protone.common.baseType.DPI
import com.protone.common.context.MApplication
import com.protone.common.utils.SCrashHandler
import com.protone.common.utils.displayUtils.Blur
import com.protone.common.utils.todayDate
import java.io.File

class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Blur.init(this)
        MApplication.init(this)
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(MApplication.app)
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

    override fun onTerminate() {
        super.onTerminate()
        ARouter.getInstance().destroy()
    }

}