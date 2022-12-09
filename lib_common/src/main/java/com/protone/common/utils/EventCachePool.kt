package com.protone.common.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.reflect.KClass

class EventCachePool<Event : Any>(
    private val lifecycleOwner: LifecycleOwner? = null,
    private val duration: Long
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    companion object {
        @JvmStatic
        fun <Event : Any> get(lifecycleOwner: LifecycleOwner? = null, duration: Long) =
            EventCachePool<Event>(lifecycleOwner, duration)
    }

    private val eventMap by lazy { mutableMapOf<KClass<out Event>, MutableList<Event>>() }



    fun holdEvent(event: Event) {

        eventMap[event::class].takeIf {
            if (it == null) eventMap[event::class] = mutableListOf()
            true
        }?.add(event)

    }

    private fun createJob(): Job = launch(start = CoroutineStart.LAZY) {
        while (isActive){
            if (lifecycleOwner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) return@launch
            delay(duration)
            dispatch()
        }
    }

    private suspend fun dispatch() {
        eventMap.forEach { (_, v) ->
            block?.invoke(v)
            v.clear()
        }
    }

    private var block: (suspend (List<Event>) -> Unit)? = null
    fun handlerEvent(block: suspend (List<Event>) -> Unit): EventCachePool<Event> {
        this.block = block
        return this
    }

    fun clear() {
        block = null
        eventMap.clear()
        cancel()
    }
}