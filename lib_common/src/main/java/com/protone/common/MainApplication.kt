package com.protone.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import androidx.constraintlayout.helper.widget.Layer
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
//        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
//            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//                activity.window.decorView.apply {
//                    setLayerType(Layer.LAYER_TYPE_HARDWARE, Paint().also {
//                        it.colorFilter =
//                            ColorMatrixColorFilter(ColorMatrix().also { cm -> cm.setSaturation(0f) })
//                    })
//                }
//            }
//
//            override fun onActivityStarted(activity: Activity) {
//            }
//
//            override fun onActivityResumed(activity: Activity) {
//            }
//
//            override fun onActivityPaused(activity: Activity) {
//            }
//
//            override fun onActivityStopped(activity: Activity) {
//            }
//
//            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//            }
//
//            override fun onActivityDestroyed(activity: Activity) {
//            }
//
//        })
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