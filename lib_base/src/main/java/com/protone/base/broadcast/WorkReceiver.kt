package com.protone.base.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.protone.common.context.MApplication
import com.protone.common.context.UPDATE_GALLERY
import com.protone.common.context.UPDATE_MUSIC
import com.protone.common.utils.json.toUri
import com.protone.common.utils.tryWithRecording

val workLocalBroadCast by lazy { LocalBroadcastManager.getInstance(MApplication.app) }

abstract class WorkReceiver : BroadcastReceiver(), IWorkService {

    override fun onReceive(p0: Context?, p1: Intent?) {
        tryWithRecording {
            when (p1?.action) {
                UPDATE_MUSIC -> updateMusic(p1.getStringExtra("uri")?.toUri())
                UPDATE_GALLERY -> updateGallery(p1.getStringExtra("uri")?.toUri())
            }
        }
    }
}

interface IWorkService {
    fun updateMusic(data: Uri?)
    fun updateGallery(data: Uri?)
}