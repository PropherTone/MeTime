package com.protone.component

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.databinding.ViewDataBinding
import com.protone.common.context.intent
import com.protone.common.context.isServiceRunning
import com.protone.component.activity.BaseMsgActivity
import com.protone.component.service.MusicBinder
import com.protone.component.service.MusicService
import com.protone.component.service.observeServiceState
import com.protone.component.view.customView.musicPlayer.bitmapCachePool
import kotlinx.coroutines.launch

abstract class BaseMusicActivity<VB : ViewDataBinding, VM : BaseViewModel, VE : BaseViewModel.ViewEvent>
    : BaseMsgActivity<VB, VM, VE>() {

    private var serviceConnection: ServiceConnection? = null

    fun bindMusicService(block: suspend (MusicBinder) -> Unit) {
        if (!isServiceRunning(MusicService::class.java)) {
            observeServiceState(this, MusicService::class.java) {
                connectService(block)
            }
            startService(MusicService::class.intent)
        } else {
            connectService(block)
        }
    }

    private fun connectService(block: suspend (MusicBinder) -> Unit) {
        serviceConnection = (object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                launch {
                    block(p1 as MusicBinder)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) = Unit

        }).also { conn ->
            bindService(MusicService::class.intent, conn, BIND_AUTO_CREATE)
        }
    }

    override fun onTrimMemory(level: Int) {
        if (level > TRIM_MEMORY_MODERATE) {
            bitmapCachePool.clear()
        }
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        serviceConnection?.let { unbindService(it) }
        super.onDestroy()
    }
}