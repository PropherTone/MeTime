package com.protone.config.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.getParentPath
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.baseType.withIOContext
import com.protone.common.utils.SCrashHandler
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.R
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UserConfigViewModel : BaseViewModel() {

    sealed class UserConfigEvent : ViewEvent {
        object Login : UserConfigEvent()
        object Icon : UserConfigEvent()
        object Name : UserConfigEvent()
        object PassWord : UserConfigEvent()
        object Share : UserConfigEvent()
        object Lock : UserConfigEvent()
        object Unlock : UserConfigEvent()
        object Refresh : UserConfigEvent()
        object ClearCache : UserConfigEvent()
        object Log : UserConfigEvent()
        object CombineGallery : UserConfigEvent()
        object DispatchGallery : UserConfigEvent()
    }

    enum class DisplayMode {
        UnRegis,
        Locked,
        Normal,
        CombineGallery
    }

    private var onClear = false

    fun deleteOldIcon(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    suspend fun clearCache() {
        if (onClear) return
        onClear = true
        withIOContext {
            try {
                Image.apply {
                    clearDiskCache()
                    withContext(Dispatchers.Main) {
                        clearMemory()
                    }
                }
                SCrashHandler.path?.getParentPath()?.let { path ->
                    val file = File(path)
                    if (file.exists() && file.isDirectory) {
                        file.listFiles()?.forEach {
                            it.delete()
                        }
                    }
                }
                R.string.success.getString().toast()
            } catch (e: Exception) {
                R.string.none.getString().toast()
            }
            onClear = false
        }
    }

}