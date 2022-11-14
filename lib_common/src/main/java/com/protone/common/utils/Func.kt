package com.protone.common.utils

import android.content.pm.ApplicationInfo
import android.os.Looper
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.MApplication
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

fun isInDebug(): Boolean {
    return try {
        val info: ApplicationInfo = MApplication.app.applicationInfo
        (info.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    } catch (e: java.lang.Exception) {
        false
    }
}

fun todayDate(format: String): String = SimpleDateFormat(
    format,
    Locale.getDefault()
).format(Calendar.getInstance(Locale.getDefault()).apply {
    timeInMillis = System.currentTimeMillis()
}.time)

fun tryWithRecording(func: () -> Unit) {
    try {
        func.invoke()
    } catch (e: Exception) {
        if (SCrashHandler.path != null) {
            SCrashHandler.writeLog(e)
        }
    }
}

suspend inline fun <T> onResult(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline runnable: CoroutineScope.(CancellableContinuation<T>) -> Unit
) = withContext(dispatcher) {
    suspendCancellableCoroutine {
        try {
            runnable.invoke(this, it)
        } catch (e: Exception) {
            if (isInDebug()) e.printStackTrace()
            R.string.unknown_error.getString().toast()
        }
    }
}

inline fun onBackground(crossinline function: () -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        Thread {
            function.invoke()
        }.start()
    } else function.invoke()
}
