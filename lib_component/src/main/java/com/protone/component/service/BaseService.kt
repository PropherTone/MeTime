package com.protone.component.service

import android.app.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

val serviceObserver = MutableSharedFlow<String>()

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