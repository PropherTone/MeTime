package com.protone.common.utils

import java.lang.ref.WeakReference
import java.util.*

object IntentDataHolder {
    private val data = LinkedList<WeakReference<Any?>>()

    fun put(data: Any?) {
        val weakReference = WeakReference(data)
        IntentDataHolder.data.offer(weakReference)
    }

    fun get(): Any? = data.poll()?.get()

}