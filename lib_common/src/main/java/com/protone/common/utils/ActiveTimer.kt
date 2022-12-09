package com.protone.common.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class ActiveTimer(lifecycleOwner: LifecycleOwner? = null, private val delay: Long = 500L) {
    private var timerHandler: Handler? = Handler(Looper.getMainLooper()) {
        if (lifecycleOwner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) {
            destroy()
            return@Handler true
        }
        funcMap[it.what]?.invoke(it.obj)
        false
    }

    private val funcMap = mutableMapOf<Int, (Any?) -> Unit>()

    fun addFunction(key: Int, func: (Any?) -> Unit) {
        funcMap[key] = func
    }

    fun block(key: Int) {
        timerHandler?.removeMessages(key)
    }

    fun active(key: Int, msg: Any? = null) {
        block(key)
        if (msg != null) {
            timerHandler?.sendMessageDelayed(Message().apply {
                what = key
                this.obj = msg
            }, delay)
        } else {
            timerHandler?.sendEmptyMessageDelayed(key, delay)
        }
    }

    fun destroy() {
        funcMap.clear()
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
    }
}