package com.protone.config.viewModel

import com.protone.common.baseType.getParentPath
import com.protone.common.utils.SCrashHandler
import com.protone.common.utils.spans.toBase64
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

class LogViewModel : BaseViewModel() {

    fun getLogContent(path:String): String {
        val file = File(path)
        val fileReader = FileReader(file)
        return fileReader.readText().toBase64()
    }

    suspend fun getLogs(): MutableList<String>? {
       return SCrashHandler.path?.let { path ->
            withContext(Dispatchers.IO) {
                val file = File(path.getParentPath())
                val logs = mutableListOf<String>()
                file.listFiles()?.forEach { log ->
                    log?.path?.let { logPath -> logs.add(logPath) }
                }
                logs
            }
        }
    }
}