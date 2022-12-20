package com.protone.component

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.protone.common.baseType.DPI
import com.protone.common.baseType.launchDefault
import com.protone.common.context.MApplication
import com.protone.common.context.intent
import com.protone.common.utils.SCrashHandler
import com.protone.common.utils.displayUtils.Blur
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.todayDate
import com.protone.component.BuildConfig
import com.protone.component.database.dao.DatabaseBridge
import com.protone.component.service.MusicService
import com.protone.component.service.WorkService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.File

class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Blur.init(this)
        MApplication.init(this)
        startService(WorkService::class.intent)
        startService(MusicService::class.intent)
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(MApplication.app)
        MainScope().launchDefault {
            DPI = MApplication.app.resources.displayMetrics.densityDpi
            val file = File("${base?.externalCacheDir?.path}/CrashLog")
            val result = if (!file.exists()) {
                file.mkdirs()
            } else true
            val todayDate = todayDate("yyyy_MM_dd_HH_mm_ss")
            SCrashHandler.path =
                if (result) "${base?.externalCacheDir?.path}/CrashLog/s_crash_log_${todayDate}.txt"
                else "${base?.externalCacheDir?.path}/s_crash_log_${todayDate}.txt"
            cancel()
        }
    }

    override fun onLowMemory() {
        Image.clearMemory()
        super.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
        stopService(MusicService::class.intent)
        stopService(WorkService::class.intent)
        DatabaseBridge.instance.shutdownNow()
        ARouter.getInstance().destroy()
    }

}