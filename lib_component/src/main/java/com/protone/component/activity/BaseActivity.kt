package com.protone.component.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.launchMain
import com.protone.common.context.*
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.TAG
import com.protone.common.utils.onResult
import com.protone.component.BaseViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseActivity<VB : ViewDataBinding, VM : BaseViewModel, VE : BaseViewModel.ViewEvent>
    : AppCompatActivity(), CoroutineScope by MainScope() {

    protected abstract val viewModel: VM
    protected lateinit var binding: VB
        private set

    abstract fun createView(): VB
    abstract suspend fun VM.init()

    protected var onResume: (suspend () -> Unit)? = null
        set(value) {
            field = value
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                launch {
                    field?.invoke()
                }
            }
        }
    private var onRestart: (suspend () -> Unit)? = null
    private var onPause: (suspend () -> Unit)? = null
    private var onStop: (suspend () -> Unit)? = null
    private var onFinish: (suspend () -> Unit)? = null

    val code = AtomicInteger(0)

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
        setTransparentClipStatusBar()
        launchDefault {
            ARouter.getInstance().inject(this)
            activityOperationBroadcast.registerReceiver(
                activityOperationReceiver,
                IntentFilter(ACTIVITY_FINISH)
            )
        }
        super.onCreate(savedInstanceState)
        binding = createView().apply {
            setContentView(root)
            root.onGlobalLayout {
                lifecycleScope.launchWhenStarted {
                    viewModel.init()
                }
            }
        }
    }

    fun onLifecycleEvent(block: LifecycleEvent.() -> Unit) {
        LifecycleEvent().apply(block)
    }

    fun <T> startActivityWithGainData(data: T, intent: Intent?) {
        IntentDataHolder.put(data)
        startActivity(intent)
    }

    inline fun <T> startActivityWithGainData(
        data: T,
        routerPath: String,
        crossinline postCard: Postcard.() -> Postcard
    ) {
        IntentDataHolder.put(data)
        ARouter.getInstance().build(routerPath).postCard().navigation()
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

    private val _activityResultMessenger by lazy { MutableSharedFlow<Intent?>() }
    val activityResultMessenger by lazy { _activityResultMessenger.asSharedFlow() }

    inline fun startActivity(routerPath: String, postCard: Postcard.() -> Postcard) {
        ARouter.getInstance().build(routerPath).postCard().navigation(this)
    }

    fun startActivity(routerPath: String) {
        ARouter.getInstance().build(routerPath).navigation(this)
    }

    suspend inline fun startActivityForResult(
        routerPath: String,
        crossinline postCard: Postcard.() -> Postcard,
    ) = onResult { co ->
        ARouter.getInstance()
            .build(routerPath)
            .postCard()
            .navigation(this@BaseActivity, code.incrementAndGet())
        var job: Job? = null
        job = launchMain {
            activityResultMessenger.collect {
                co.resumeWith(Result.success(it))
                job?.cancel()
            }
        }
        job.start()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == code.get()) {
            launch { _activityResultMessenger.emit(data) }
        }
    }

    protected fun View.fitStatuesBar() {
        this.marginTop(statuesBarHeight)
    }

    protected fun View.fitNavigationBar() {
        if (hasNavigationBar) this.marginBottom(navigationBarHeight)
    }

    protected fun View.fitStatuesBarUsePadding() {
        this.paddingTop(statuesBarHeight)
    }

    protected fun View.fitNavigationBarUsePadding() {
        if (isNavigationBar) this.paddingBottom(navigationBarHeight)
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
                onResume?.invoke() ?: doResume()
            }
        } finally {
            super.onResume()
        }
    }

    override fun onRestart() {
        try {
            launch {
                Log.d(TAG, "onRestart: ${this@BaseActivity::class.simpleName}")
                onRestart?.invoke() ?: doRestart()
            }
        } finally {
            super.onRestart()
        }
    }

    override fun onPause() {
        try {
            launch {
                Log.d(TAG, "onPause: ${this@BaseActivity::class.simpleName}")
                onPause?.invoke() ?: doPause()
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
                onStop?.invoke() ?: doStop()
            }
        } finally {
            super.onStop()
        }
    }

    open fun getSwapAnim(): Pair<Int, Int>? {
        return null
    }

    override fun finish() {
        try {
            launch {
                Log.d(TAG, "finish: ${this@BaseActivity::class.simpleName}")
                onFinish?.invoke() ?: doFinish()
                getSwapAnim()?.let {
                    overridePendingTransition(it.first, it.second)
                }
            }
        } finally {
            super.finish()
        }
    }

    override fun onDestroy() {
        try {
            Log.d(TAG, "onDestroy: ${this@BaseActivity::class.simpleName}")
            activityOperationBroadcast.unregisterReceiver(activityOperationReceiver)
            binding.unbind()
        } finally {
            cancel()
            super.onDestroy()
        }
    }

    inner class LifecycleEvent {
        fun onFinish(block: suspend () -> Unit) {
            onFinish = block
        }

        fun onResume(block: suspend () -> Unit) {
            onResume = block
        }

        fun onRestart(block: suspend () -> Unit) {
            onRestart = block
        }

        fun onPause(block: suspend () -> Unit) {
            onPause = block
        }

        fun onStop(block: suspend () -> Unit) {
            onStop = block
        }
    }


}