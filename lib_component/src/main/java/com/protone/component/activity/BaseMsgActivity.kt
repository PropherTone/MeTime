package com.protone.component.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.launchMain
import com.protone.common.baseType.toast
import com.protone.common.context.ACTIVITY_FINISH
import com.protone.common.context.ACTIVITY_RESTART
import com.protone.common.context.activityOperationBroadcast
import com.protone.common.context.finishAll
import com.protone.component.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseMsgActivity<VB : ViewDataBinding, VM : BaseViewModel, VE : BaseViewModel.ViewEvent>
    : BaseActivity<VB, VM, VE>() {

    private var onViewEvent: (suspend (VE) -> Unit)? = null
        set(value) {
            field = value
            viewEventTask?.start()
        }

    fun onViewEvent(block: suspend (VE) -> Unit) {
        onViewEvent = block
    }

    private var viewEvent: Channel<VE>? = null
    private var viewEventTask: Job? = null

    init {
        viewEvent = Channel(1)
        viewEventTask = launchMain {
            viewEvent?.receiveAsFlow()?.collect {
                try {
                    onViewEvent?.invoke(it)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                    com.protone.common.R.string.unknown_error.getString().toast()
                }
            }
        }
    }

    private val activityOperationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTIVITY_FINISH -> {
                    finishAll()
                }
                ACTIVITY_RESTART -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        launchDefault {
            ARouter.getInstance().inject(this)
            activityOperationBroadcast.registerReceiver(
                activityOperationReceiver,
                IntentFilter(ACTIVITY_FINISH)
            )
        }
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        try {
            activityOperationBroadcast.unregisterReceiver(activityOperationReceiver)
        } finally {
            super.onDestroy()
        }
    }

    fun sendViewEvent(event: VE) {
        viewEvent?.trySend(event)
    }

    fun closeEvent() {
        viewEventTask?.cancel()
        viewEventTask = null
        viewEvent?.close()
        viewEvent = null
    }
}