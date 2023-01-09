package com.protone.component.service

import android.app.Service
import com.protone.common.baseType.launchDefault
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

val serviceObserver = MutableSharedFlow<String>()

inline fun observeServiceStatues(
    scope: CoroutineScope,
    serviceClazz: Class<out Service>,
    crossinline block: () -> Unit
) {
    scope.launchDefault {
        serviceObserver.collect {
            when (it) {
                "CREATE:${serviceClazz.name}" -> block()
            }
            cancel()
        }
    }
}

abstract class BaseService : Service(), CoroutineScope by CoroutineScope(Dispatchers.Default) {

    override fun onCreate() {
        super.onCreate()
        launch {
            serviceObserver.emit("CREATE:${this@BaseService::class.java.name}")
        }
    }

    override fun onDestroy() {
        launch {
            serviceObserver.emit("DESTROY:${this@BaseService::class.java.name}")
        }
        super.onDestroy()
        cancel()
    }
}