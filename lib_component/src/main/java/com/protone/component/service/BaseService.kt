package com.protone.component.service

import android.app.Service
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.util.concurrent.LinkedBlockingDeque

private val mutableServiceObserver = MutableLiveData<String>()
val serviceObserver: LiveData<String>
    get() = mutableServiceObserver

inline fun LifecycleOwner.observeServiceState(
    serviceClazz: Class<out Service>,
    crossinline block: () -> Unit
) {
    serviceObserver.observe(this) {
        when (it) {
            "CREATE:${serviceClazz.name}" -> {
                block()
                serviceObserver.removeObservers(this)
            }
        }
    }
}

fun isServiceRunning(clazz: Class<*>): Boolean {
    return BaseService.runningServices.contains(clazz.name)
//    (getSystemService(Service.ACTIVITY_SERVICE) as android.app.ActivityManager?).let {
//        return it?.getRunningServices(Int.MAX_VALUE)?.find { runningServiceInfo ->
//            runningServiceInfo.service.className == clazz.name
//        } != null
//    }
}

abstract class BaseService : Service(), CoroutineScope by CoroutineScope(Dispatchers.Default) {

    companion object {
        @JvmStatic
        val runningServices = LinkedBlockingDeque<String>()
    }

    override fun onCreate() {
        super.onCreate()
        runningServices.offer(this@BaseService::class.java.name)
        mutableServiceObserver.postValue("CREATE:${this@BaseService::class.java.name}")
    }

    override fun onDestroy() {
        runningServices.remove(this@BaseService::class.java.name)
        mutableServiceObserver.postValue("DESTROY:${this@BaseService::class.java.name}")
        super.onDestroy()
        cancel()
    }
}