package com.protone.config.activity

import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.config.databinding.LogActivityBinding
import com.protone.common.R
import com.protone.common.baseType.getFileName
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.root
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteEditWire.noteEditPostcard
import com.protone.common.utils.TAG
import com.protone.component.BaseActivity
import com.protone.component.BaseViewModel
import com.protone.component.database.dao.DatabaseBridge
import com.protone.component.view.adapter.LogListAdapter
import com.protone.component.view.dialog.titleDialog
import com.protone.config.viewModel.LogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Route(path = RouterPath.ConfigRouterPath.Log)
class LogActivity : BaseActivity<LogActivityBinding, LogViewModel, BaseViewModel.ViewEvent>(false) {
    override val viewModel: LogViewModel by viewModels()

    override fun createView(): LogActivityBinding {
        return LogActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@LogActivity
            root.fitStatuesBar()
        }
    }

    override suspend fun LogViewModel.init() {
        setLogEvent(object : LogListAdapter.LogEvent {
            override fun shareLog(path: String) {
                startActivity(Intent(Intent.ACTION_SEND).apply {
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            this@LogActivity,
                            "com.protone.MeTime.fileProvider",
                            File(path)
                        )
                    )
                    type = "text/plain"
                })
            }

            override fun viewLog(path: String) {
                launch(Dispatchers.IO) {
                    try {
                        startActivityWithGainData(
                            getLogContent(path),
                            RouterPath.NoteRouterPath.Edit
                        ) { noteEditPostcard(path.getFileName()) }
                    } catch (e: Exception) {
                        R.string.failed_msg.getString().toast()
                    }
                }
            }

        })
    }

    override suspend fun doStart() {
        viewModel.initLogList()
    }

    fun action() {
        val randomCode = (0..10000).random().toString()
        Log.d(TAG, "--=====<randomCode here: $randomCode>=====--")
        titleDialog(R.string.password.getString(), "") {
            if (it == randomCode) {
                DatabaseBridge.instance.showDataBase(this)
            } else {
                R.string.wrong_password.getString().toast()
            }
        }
    }

    private suspend fun LogViewModel.initLogList() {
        getLogs()?.let { (binding.logList.adapter as LogListAdapter).initLogs(it) }
    }

    private fun setLogEvent(event: LogListAdapter.LogEvent) {
        binding.logList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LogListAdapter(context)
        }
        (binding.logList.adapter as LogListAdapter).logEvent = event
    }
}