package com.protone.component.activity

import androidx.databinding.ViewDataBinding
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchMain
import com.protone.common.baseType.toast
import com.protone.common.context.showFailedToast
import com.protone.component.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseMsgActivity<VB : ViewDataBinding, VM : BaseViewModel, VE : BaseViewModel.ViewEvent>(
    receiveEvent: Boolean = true
) : BaseActivity<VB, VM, VE>() {

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
        if (receiveEvent) {
            viewEvent = Channel(1)
            viewEventTask = launchMain {
                viewEvent?.receiveAsFlow()?.collect {
                    try {
                        onViewEvent?.invoke(it)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        showFailedToast()
                    }
                }
            }
        }
    }

    fun sendViewEvent(event: VE) {
        launch { viewEvent?.send(event) }
    }

    private fun closeEvent() {
        viewEventTask?.cancel()
        viewEventTask = null
        viewEvent?.close()
        viewEvent = null
    }

    override fun onDestroy() {
        try {
            closeEvent()
        } finally {
            super.onDestroy()
        }
    }

}