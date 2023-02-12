package com.protone.component.service

import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.protone.common.context.*
import com.protone.common.entity.Music
import com.protone.common.entity.NO_MUSIC
import com.protone.common.entity.getEmptyMusic
import com.protone.common.media.musicPlayer.MusicPlay
import com.protone.common.media.musicPlayer.MusicPlayer
import com.protone.common.utils.json.toJson
import com.protone.component.MusicControllerIMP.Companion.LOOP_LIST
import com.protone.component.MusicControllerIMP.Companion.LOOP_SINGLE
import com.protone.component.MusicControllerIMP.Companion.NO_LOOP
import com.protone.component.MusicControllerIMP.Companion.PLAY_LIST
import com.protone.component.MusicControllerIMP.Companion.RANDOM
import com.protone.component.broadcast.ApplicationBroadCast
import com.protone.component.broadcast.MusicReceiver
import com.protone.component.broadcast.musicBroadCastManager
import com.protone.component.database.userConfig
import com.protone.component.notification.music.IMusicNotification
import com.protone.component.notification.music.MusicNotification
import com.protone.component.view.customView.musicPlayer.getAlbumBitmap
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

/**
 * MusicService by ProTone 2022/03/23
 */
class MusicService : BaseService(), IMusicService {

    private val musicNotification: IMusicNotification by lazy {
        MusicNotification(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
    }

    private val musicPlayer: MusicPlay by lazy {
        MusicPlayer().apply {
            init(this@MusicService)
            onPlayState {
                onStart {
                    currentMusic.postValue(rightMusic)
                    playState.postValue(true)
                }
                onPause {
                    playState.postValue(false)
                }
                onCompletion {
                    playState.postValue(false)
                    when (loopModeLive.value) {
                        LOOP_LIST -> musicBroadCastManager.sendBroadcast(Intent(MUSIC_NEXT))
                        LOOP_SINGLE -> {
                            playPosition.decrementAndGet()
                            musicBroadCastManager.sendBroadcast(Intent(MUSIC_NEXT))
                        }
                        NO_LOOP -> musicBroadCastManager.sendBroadcast(Intent(MUSIC_PAUSE))
                        RANDOM -> {
                            playPosition.set((0 until playList.size - 1).random())
                            repeat(2) {
                                musicBroadCastManager.sendBroadcast(Intent(MUSIC_PLAY))
                            }
                        }
                        PLAY_LIST -> {
                            if (playPosition.get() <= playList.size - 1)
                                musicBroadCastManager.sendBroadcast(Intent(MUSIC_NEXT))
                            else musicBroadCastManager.sendBroadcast(Intent(MUSIC_PAUSE))
                        }
                    }
                }
                onProgress {
                    this@MusicService.progress.postValue(it)
                }
            }
        }
    }

    private var playPosition = AtomicInteger(0)
    private val playList = LinkedBlockingDeque<Music>()
    private val rightMusic: Music get() = playList.elementAt(playPosition.get())

    private val progress = MutableLiveData<Long>()
    private val playState = MutableLiveData<Boolean>()
    private val currentMusic = MutableLiveData<Music>()
    private val loopModeLive = MutableLiveData<Int>()

    private val appReceiver = object : ApplicationBroadCast() {
        private var isDestroy = false
        override fun finish() {
            if (!isDestroy) {
                isDestroy = true
                MApplication.app.stopService(MusicService::class.intent)
                onDestroy()
                activityOperationBroadcast.sendBroadcast(Intent(ACTIVITY_FINISH))
            }
        }
    }

    private val receiver = object : MusicServiceReceiver(this) {

        override fun refresh(b: Boolean, ref: Boolean): Unit = notificationPlayState(b, ref)

        private fun notificationPlayState(state: Boolean, ref: Boolean) {
            launch {
                runCatching {
                    if (playList.isEmpty()) return@launch
                    playState.postValue(state)
                    musicNotification.initNotification(this@MusicService)
                    musicNotification.setPlayState(state)
                    if (state || ref) {
                        val music = rightMusic
                        musicNotification.setTitle(music.title)
                        music.uri.getAlbumBitmap()?.let { ba ->
                            musicNotification.setMusicCover(ba)
                        }
                    }
                    musicNotification.doNotify()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        musicBroadCastManager.registerReceiver(receiver, musicIntentFilter)
        registerReceiver(receiver, musicIntentFilter)
        registerReceiver(appReceiver, appIntentFilter)
    }

    override fun onBind(intent: Intent?): IBinder {
        if (playList.isNotEmpty())
            currentMusic.postValue(rightMusic)
        return MusicBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            startForeground(
                0x01,
                musicNotification.initNotification(this).apply { musicNotification.doNotify() }
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        finishMusic()
        try {
            unregisterReceiver(receiver)
            unregisterReceiver(appReceiver)
            musicBroadCastManager.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.d("TAG", "onDestroy")
        }
        musicNotification.cancelAll()
        musicNotification.release()
        userConfig.lastMusic = onMusicPlaying().value?.toJson() ?: ""
        userConfig.lastMusicProgress = progress.value ?: 0L
        super.onDestroy()
        cancel()
    }

    override fun setLoopMode(mode: Int) {
        loopModeLive.postValue(mode)
    }

    override fun play(music: Music?) {
        if (music != null) {
            if (playList.isEmpty()) {
                currentMusic.postValue(getEmptyMusic())
                return
            }
            if (music !in playList) {
                playList.add(music)
            }
            val index = playList.indexOf(music)
            playPosition.set(index)
            currentMusic.postValue(music)
            musicPlayer.play(music)
            return
        }
        if (playList.isEmpty()) return
        currentMusic.value.let {
            musicPlayer.play(it)
            currentMusic.postValue(it)
        }
    }

    override fun pause() {
        musicPlayer.pause()
    }

    override fun next() {
        if (playList.isEmpty()) return
        launch {
            if (playPosition.incrementAndGet() > playList.size - 1) playPosition.set(0)
            finishMusic()
            play(rightMusic)
        }
    }

    override fun previous() {
        if (playList.isEmpty()) return
        launch {
            if (playPosition.decrementAndGet() <= 0) playPosition.set(playList.size - 1)
            finishMusic()
            play(rightMusic)
        }
    }

    override fun getPlayList(): List<Music> {
        return (playList.toList()).ifEmpty { mutableListOf(getEmptyMusic()) }
    }

    override fun setPlayList(list: List<Music>) {
        playList.clear()
        playList.addAll(list)
        Log.d("TAG", "setPlayList: ${playList.size}")
    }

    override fun isPlaying(): Boolean = playState.value == true

    override fun onProgress(): LiveData<Long> = progress

    override fun onPlayState(): LiveData<Boolean> = playState

    override fun onLoopMode(): LiveData<Int> = loopModeLive

    override fun setProgress(progress: Long) {
        musicPlayer.seekTo(progress)
    }

    override fun onMusicPlaying(): LiveData<Music> = currentMusic

    override fun init(music: Music, progress: Long) {
        Log.d("TAG", "init: ")
        currentMusic.postValue(
            if (playList.isNotEmpty() && music.title != NO_MUSIC) {
                val index = playList.indexOf(music)
                if (index == -1) {
                    playPosition.set(0)
                    getEmptyMusic()
                } else {
                    playPosition.set(index)
                    rightMusic.also {
                        musicPlayer.prepare(it)
                    }
                }
            } else getEmptyMusic()
        )
    }

    override fun finishMusic() {
        musicPlayer.release()
    }

}

class MusicBinder(private val iMusic: IMusicService) : Binder(), IMusicService {
    override fun setLoopMode(mode: Int) = iMusic.setLoopMode(mode)
    override fun play(music: Music?) = iMusic.play(music)
    override fun pause() = iMusic.pause()
    override fun next() = iMusic.next()
    override fun previous() = iMusic.previous()
    override fun finishMusic() = iMusic.finishMusic()
    override fun getPlayList(): List<Music> = iMusic.getPlayList()
    override fun setPlayList(list: List<Music>) = iMusic.setPlayList(list)
    override fun isPlaying(): Boolean = iMusic.isPlaying()
    override fun onProgress(): LiveData<Long> = iMusic.onProgress()
    override fun onPlayState(): LiveData<Boolean> = iMusic.onPlayState()
    override fun onLoopMode(): LiveData<Int> = iMusic.onLoopMode()
    override fun setProgress(progress: Long) = iMusic.setProgress(progress)
    override fun onMusicPlaying(): LiveData<Music> = iMusic.onMusicPlaying()
    override fun init(music: Music, progress: Long) = iMusic.init(music, progress)
}

internal abstract class MusicServiceReceiver(private val iMusic: IMusicService) : MusicReceiver() {

    override fun play() {
        iMusic.isPlaying().let {
            if (!it) {
                iMusic.play()
            } else {
                pause()
            }
            refresh(it)
        }
    }

    override fun pause() = iMusic.pause()
    override fun finish() = iMusic.finishMusic()
    override fun previous() = iMusic.previous()
    override fun next() = iMusic.next()
}

interface IMusicService {
    fun setLoopMode(mode: Int)
    fun play(music: Music? = null)
    fun pause()
    fun next()
    fun previous()
    fun finishMusic()
    fun getPlayList(): List<Music>
    fun setPlayList(list: List<Music>)
    fun isPlaying(): Boolean
    fun onProgress(): LiveData<Long>
    fun onPlayState(): LiveData<Boolean>
    fun onLoopMode(): LiveData<Int>
    fun setProgress(progress: Long)
    fun onMusicPlaying(): LiveData<Music>
    fun init(music: Music, progress: Long)
}