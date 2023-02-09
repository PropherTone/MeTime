package com.protone.config.viewModel

import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.protone.common.baseType.*
import com.protone.common.context.MApplication
import com.protone.common.utils.SCrashHandler
import com.protone.component.BaseViewModel
import com.protone.component.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                Glide.get(MApplication.app).apply {
                    clearDiskCache()
                    withMainContext {
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