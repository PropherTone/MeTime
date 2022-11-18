package com.protone.component

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.databinding.ViewDataBinding
import com.protone.common.context.intent
import com.protone.common.context.musicIntentFilter
import com.protone.component.broadcast.MusicReceiver
import com.protone.component.service.MusicBinder
import com.protone.component.service.MusicService
import kotlinx.coroutines.launch

abstract class BaseMusicActivity<VB : ViewDataBinding, VM : BaseViewModel, VE : BaseViewModel.ViewEvent>(
    handleEvent: Boolean
) : BaseActivity<VB, VM, VE>(handleEvent) {

    private var serviceConnection: ServiceConnection? = null

    var musicReceiver: MusicReceiver? = null
        set(value) {
            value?.let { registerReceiver(it, musicIntentFilter) }
            field = value
        }

    fun bindMusicService(block: suspend (MusicBinder) -> Unit) {
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

    override fun onDestroy() {
        serviceConnection?.let { unbindService(it) }
        musicReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }
}