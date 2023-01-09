package com.protone.component.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.protone.common.context.FINISH

abstract class ApplicationBroadCast : BroadcastReceiver() {

    abstract fun finish()

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            FINISH -> finish()
        }
    }
}