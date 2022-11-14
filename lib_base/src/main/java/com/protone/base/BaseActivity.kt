package com.protone.base

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.protone.api.baseType.getString
import com.protone.api.baseType.toast
import com.protone.api.context.*
import com.protone.common.baseType.launchMain
import com.protone.common.context.*
import com.protone.common.utils.TAG
import com.protone.common.utils.onResult
import com.protone.base.broadcast.MusicReceiver
import com.protone.base.service.MusicBinder
import com.protone.base.service.MusicService
import com.protone.worker.IntentDataHolder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseActivity<VB : ViewDataBinding, VM : BaseViewModel, T : BaseViewModel.ViewEvent>(
    handleEvent: Boolean
) : AppCompatActivity(), CoroutineScope by MainScope() {

    protected abstract val viewModel: VM
    protected lateinit var binding: VB
        private set

    abstract fun createView(): VB
    abstract suspend fun VM.init()
    private var onViewEvent: (suspend (T) -> Unit)? = null
        set(value) {
            field = value
            viewEventTask?.start()
        }

    fun onViewEvent(block: suspend (T) -> Unit) {
        onViewEvent = block
    }

    private var viewEvent: Channel<T>? = null
    private var viewEventTask: Job? = null

    protected var onFinish: (suspend () -> Unit)? = null
    protected var onResume: (suspend () -> Unit)? = null
    protected var onRestart: (suspend () -> Unit)? = null
    protected var onPause: (suspend () -> Unit)? = null
    protected var onStop: (suspend () -> Unit)? = null

    init {
        if (handleEvent) {
            viewEvent = Channel(Channel.UNLIMITED)
            viewEventTask = launchMain {
                viewEvent?.receiveAsFlow()?.collect {
                    try {
                        onViewEvent?.invoke(it)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        R.string.come_up_unknown_error.getString().toast()
                    }
                }
            }
        }
    }

    val code = AtomicInteger(0)

    var musicReceiver: MusicReceiver? = null
        set(value) {
            value?.let { registerReceiver(it, musicIntentFilter) }
            field = value
        }

    private val activityOperationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTIVITY_FINISH -> finish()
                ACTIVITY_RESTART -> {
                    startActivity(SplashActivity::class.intent)
                }
            }
        }
    }

    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTransparentClipStatusBar()
        super.onCreate(savedInstanceState)
        activityOperationBroadcast.registerReceiver(
            activityOperationReceiver,
            IntentFilter(ACTIVITY_FINISH)
        )
        binding = createView().apply {
            setContentView(root)
            root.onGlobalLayout {
                lifecycleScope.launchWhenStarted {
                    viewModel.init()
                }
            }
        }
    }


    fun <T> startActivityWithGainData(data: T, intent: Intent?) {
        IntentDataHolder.put(data)
        startActivity(intent)
    }

    inline fun <reified T> getGainData(): T? {
        return IntentDataHolder.get().let {
            if (it is T) {
                val re = it as T
                re
            } else null
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getGainListData(): List<T>? {
        return IntentDataHolder.get().let {
            if (it is List<*> && it.size > 0 && it[0] is T) {
                val list = it as List<T>
                list
            } else null
        }
    }

    fun <T> putGainIntentData(data: T) {
        IntentDataHolder.put(data)
    }

    suspend inline fun startActivityForResult(
        intent: Intent?,
    ): ActivityResult? = onResult(Dispatchers.Main) { co ->
        activityResultRegistry.register(
            code.incrementAndGet().toString(),
            ActivityResultContracts.StartActivityForResult(),
        ) {
            co.resumeWith(Result.success(it))
        }.launch(intent)
    }

    fun sendViewEvent(event: T) {
        viewEvent?.trySend(event)
    }

    fun closeEvent() {
        viewEventTask?.cancel()
        viewEventTask = null
        viewEvent?.close()
        viewEvent = null
    }

    fun bindMusicService(block: suspend (MusicBinder) -> Unit) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                launch {
                    block(p1 as MusicBinder)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
            }

        }
        serviceConnection?.let {
            bindService(MusicService::class.intent, it, BIND_AUTO_CREATE)
        }
    }

    protected fun fitStatuesBar(root: View) {
        root.marginTop(statuesBarHeight)
    }

    protected fun fitNavigationBar(root: View) {
        if (hasNavigationBar) root.marginBottom(navigationBarHeight)
    }

    protected fun fitStatuesBarUsePadding(view: View) {
        view.paddingTop(statuesBarHeight)
    }

    protected fun fitNavigationBarUsePadding(view: View) {
        if (isNavigationBar) view.paddingBottom(navigationBarHeight)
    }

    open suspend fun doStart() = Unit
    open suspend fun doResume() = Unit
    open suspend fun doRestart() = Unit
    open suspend fun doPause() = Unit
    open suspend fun doStop() = Unit
    open suspend fun doFinish() = Unit

    override fun onStart() {
        try {
            launch {
                Log.d(TAG, "onStart: ${this@BaseActivity::class.simpleName}")
                doStart()
            }
        } finally {
            super.onStart()
        }
    }

    override fun onResume() {
        try {
            launch {
                Log.d(TAG, "onResume: ${this@BaseActivity::class.simpleName}")
                onResume?.invoke()
                doResume()
            }
        } finally {
            super.onResume()
        }
    }

    override fun onRestart() {
        try {
            launch {
                Log.d(TAG, "onRestart: ${this@BaseActivity::class.simpleName}")
                onRestart?.invoke()
                doRestart()
            }
        } finally {
            super.onRestart()
        }
    }

    override fun onPause() {
        try {
            launch {
                Log.d(TAG, "onPause: ${this@BaseActivity::class.simpleName}")
                onPause?.invoke()
                doPause()
            }
            hideSoftInput()
        } finally {
            super.onPause()
        }
    }

    override fun onStop() {
        try {
            launch {
                Log.d(TAG, "onStop: ${this@BaseActivity::class.simpleName}")
                onStop?.invoke()
                doStop()
            }
        } finally {
            super.onStop()
        }
    }

    override fun finish() {
        try {
            launch {
                Log.d(TAG, "finish: ${this@BaseActivity::class.simpleName}")
                onFinish?.invoke()
                doFinish()
            }
        } finally {
            super.finish()
        }
    }

    override fun onDestroy() {
        try {
            Log.d(TAG, "onDestroy: ${this@BaseActivity::class.simpleName}")
            binding.unbind()
            serviceConnection?.let { unbindService(it) }
            musicReceiver?.let { unregisterReceiver(it) }
            activityOperationBroadcast.unregisterReceiver(activityOperationReceiver)
        } finally {
            super.onDestroy()
            cancel()
        }
    }

}