package com.protone.component

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.protone.common.baseType.launchMain
import com.protone.common.utils.onResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseFragment<ViewBinding : ViewDataBinding, VM : ViewModel> : Fragment(),
    CoroutineScope by MainScope() {

    protected lateinit var binding: ViewBinding
    protected lateinit var viewModel: VM

    private val _activityResultMessenger by lazy { MutableSharedFlow<Intent?>() }
    val activityResultMessenger by lazy { _activityResultMessenger.asSharedFlow() }

    val code = AtomicInteger(0)

    abstract fun createViewModel(): VM

    abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = createViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = createBinding(inflater, container, savedInstanceState).also { binding = it }.root

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    fun startActivity(routerPath: String) {
        ARouter.getInstance().build(routerPath).navigation(context)
    }

    inline fun startActivity(routerPath: String, postCard: Postcard.() -> Postcard) {
        ARouter.getInstance().build(routerPath).postCard().navigation(activity)
    }

    suspend inline fun startActivityForResult(
        routerPath: String,
        crossinline postCard: Postcard.() -> Postcard,
    ) = onResult { co ->
        ARouter.getInstance()
            .build(routerPath)
            .postCard()
            .navigation(activity, code.incrementAndGet())
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
}