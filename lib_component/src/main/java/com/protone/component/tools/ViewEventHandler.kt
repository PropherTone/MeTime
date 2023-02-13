package com.protone.component.tools

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.protone.common.baseType.launchMain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

interface ViewEventHandle<ViewEvent> {
    fun onViewEvent(
        coroutineScope: CoroutineScope,
        lifecycleOwner: LifecycleOwner,
        block: suspend (ViewEvent) -> Unit
    )

    fun sendViewEvent(event: ViewEvent)
}

class ViewEventHandler<ViewEvent> : ViewEventHandle<ViewEvent> {

    private var onViewEvent: (suspend (ViewEvent) -> Unit)? = null
        set(value) {
            field = value
            viewEventTask?.start()
        }

    override fun onViewEvent(
        coroutineScope: CoroutineScope,
        lifecycleOwner: LifecycleOwner,
        block: suspend (ViewEvent) -> Unit
    ) {
        viewEvent = Channel(1)
        viewEventTask = coroutineScope.launchMain {
            viewEvent?.receiveAsFlow()?.collect {
                try {
                    onViewEvent?.invoke(it)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event.targetState == Lifecycle.State.DESTROYED) closeEvent()
            }
        })
        onViewEvent = block
    }

    private var viewEvent: Channel<ViewEvent>? = null
    private var viewEventTask: Job? = null

    override fun sendViewEvent(event: ViewEvent) {
        viewEvent?.trySend(event)
    }

    private fun closeEvent() {
        viewEventTask?.cancel()
        viewEventTask = null
        viewEvent?.close()
        viewEvent = null
    }

}