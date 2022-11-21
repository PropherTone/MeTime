package com.protone.common.utils

import android.os.Handler
import android.os.Looper
import android.os.Message

class ActiveTimer(private val delay: Long = 500L) {
    private var timerHandler: Handler? = Handler(Looper.getMainLooper()) {
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
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
    }
}