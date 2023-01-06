package com.protone.component.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.protone.common.context.*
import com.protone.common.entity.Music
import com.protone.common.entity.getEmptyMusic
import com.protone.common.utils.json.toJson
import com.protone.component.MusicControllerIMP.Companion.LOOP_LIST
import com.protone.component.MusicControllerIMP.Companion.LOOP_SINGLE
import com.protone.component.MusicControllerIMP.Companion.NO_LOOP
import com.protone.component.MusicControllerIMP.Companion.PLAY_LIST
import com.protone.component.MusicControllerIMP.Companion.RANDOM
import com.protone.component.R
import com.protone.component.broadcast.ApplicationBroadCast
import com.protone.component.broadcast.MusicReceiver
import com.protone.component.broadcast.musicBroadCastManager
import com.protone.component.database.userConfig
import com.protone.component.view.customView.musicPlayer.getBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.timerTask

/**
 * MusicService by ProTone 2022/03/23
 */
class MusicService : BaseService(), IMusicService, MediaPlayer.OnCompletionListener {

    companion object {
        const val MUSIC_NOTIFICATION_NAME = "MUSIC_NOTIFICATION"
        const val NOTIFICATION_ID = 0x01
    }

    private var notificationManager: NotificationManager? = null
    private var notification: Notification? = null
    private var remoteViews: RemoteViews? = null

    private var musicPlayer: MediaPlayer? = null

    private fun initMusicPlayer(): MediaPlayer? {
        if (playList.isEmpty()) return null
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(
                MApplication.app,
                playList[playPosition.get()].uri
            ).also {
                it?.setOnCompletionListener(this)
            }
        }
        return musicPlayer
    }

    private var progressTimer: Timer? = null

    private val mutex by lazy { Mutex() }

    private var playPosition = AtomicInteger(0)
    private val playList = mutableListOf<Music>()
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
            }
        }

        override fun music() {
        }

    }

    private val receiver = object : MusicServiceReceiver(this) {

        override fun refresh(b: Boolean, ref: Boolean): Unit = notificationPlayState(b, ref)

        private fun notificationPlayState(state: Boolean, ref: Boolean) {
            launch(Dispatchers.Default) {
                mutex.withLock {
                    if (playList.isEmpty()) return@launch
                    playState.postValue(state)
                    initMusicNotification()
                    withContext(Dispatchers.Main) {
                        remoteViews?.setImageViewResource(
                            R.id.notify_music_control,
                            if (state) R.drawable.ic_baseline_pause_24
                            else R.drawable.ic_baseline_play_arrow_24
                        )
                        if (state || ref) {
                            remoteViews?.setTextViewText(
                                R.id.notify_music_name,
                                playList[playPosition.get()].title
                            )
                            playList[playPosition.get()].uri.getBitmap()?.let { ba ->
                                remoteViews?.setImageViewBitmap(R.id.notify_music_icon, ba)
                            }
                        }
                    }
                    notificationManager?.notify(NOTIFICATION_ID, notification)
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
            currentMusic.postValue(playList[playPosition.get()])
        return MusicBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(0x01, initMusicNotification())
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
        notificationManager?.cancelAll()
        userConfig.lastMusic = onMusicPlaying().value?.toJson() ?: ""
        userConfig.lastMusicProgress = progress.value ?: 0L
        super.onDestroy()
        cancel()
        activityOperationBroadcast.sendBroadcast(Intent(ACTIVITY_FINISH))
    }

    private fun initMusicNotification(): Notification {
        remoteViews = RemoteViews(packageName, R.layout.music_notification_layout).apply {
            val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

            PendingIntent.getBroadcast(
                this@MusicService,
                0,
                Intent(MUSIC_PLAY),
                intentFlags
            ).let { setOnClickPendingIntent(R.id.notify_music_control, it) }

            PendingIntent.getBroadcast(
                this@MusicService,
                0,
                Intent(MUSIC_PREVIOUS),
                intentFlags
            ).let { setOnClickPendingIntent(R.id.notify_music_previous, it) }

            PendingIntent.getBroadcast(
                this@MusicService,
                0,
                Intent(MUSIC_NEXT),
                intentFlags
            ).let { setOnClickPendingIntent(R.id.notify_music_next, it) }

            PendingIntent.getBroadcast(
                this@MusicService,
                0,
                Intent(MUSIC),
                intentFlags
            ).let { setOnClickPendingIntent(R.id.notify_music_parent, it) }

            PendingIntent.getBroadcast(
                this@MusicService,
                0,
                Intent(FINISH),
                intentFlags
            ).let { setOnClickPendingIntent(R.id.notify_music_close, it) }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MUSIC_NOTIFICATION_NAME,
                "SEEN_MUSIC",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
            Notification.Builder(this, MUSIC_NOTIFICATION_NAME).apply {
                setCustomContentView(remoteViews)
                setSmallIcon(R.drawable.ic_baseline_music_note_24)
            }.build()
        } else {
            @Suppress("DEPRECATION") Notification().apply {
                contentView = remoteViews
                icon = R.drawable.ic_baseline_music_note_24
            }
        }.also {
            it.flags = Notification.FLAG_NO_CLEAR
            notification = it
            notificationManager?.notify(NOTIFICATION_ID, it)
        }
    }

    override fun setLoopMode(mode: Int) {
        loopModeLive.postValue(mode)
    }

    override fun play(music: Music?) {
        launch {
            mutex.withLock {
                (if (music != null) {
                    finishMusic()
                    if (playList.isEmpty()) {
                        currentMusic.postValue(getEmptyMusic())
                        return@launch
                    }
                    if (music !in playList) {
                        playList.add(music)
                    }
                    val index = playList.indexOf(music)
                    playPosition.set(index)
                    initMusicPlayer()
                } else musicPlayer)?.apply {
                    start()
                    currentMusic.postValue(playList[playPosition.get()])
                    playState.postValue(true)
                    if (progressTimer == null) progressTimer = Timer()
                    progressTimer?.schedule(timerTask {
                        try {
                            if (isPlaying) progress.postValue(currentPosition.toLong())
                        } catch (ignored: Exception) {
                        }
                    }, 0, 100)
                }
            }
        }
    }

    override fun pause() {
        launch {
            mutex.withLock {
                if (musicPlayer?.isPlaying == true)
                    musicPlayer?.apply {
                        pause()
                        playState.postValue(false)
                        progressTimer?.cancel()
                        progressTimer = null
                    }
            }
        }
    }

    override fun next() {
        if (playList.isEmpty()) return
        launch {
            if (playPosition.incrementAndGet() > playList.size - 1) playPosition.set(0)
            finishMusic()
            initMusicPlayer()
            play()
        }
    }

    override fun previous() {
        if (playList.isEmpty()) return
        launch {
            if (playPosition.decrementAndGet() <= 0) playPosition.set(playList.size - 1)
            finishMusic()
            initMusicPlayer()
            play()
        }
    }

    override fun getPlayList(): MutableList<Music> {
        synchronized(playList) {
            return playList.ifEmpty { mutableListOf(getEmptyMusic()) }
        }
    }

    override fun setPlayList(list: MutableList<Music>) {
        launch(Dispatchers.Default) {
            mutex.withLock(playList) {
                playList.clear()
                playList.addAll(list)
            }
        }
    }

    override fun getPlayState(): Boolean = musicPlayer?.isPlaying == true

    override fun onProgress(): LiveData<Long> = progress

    override fun onPlayState(): LiveData<Boolean> = playState

    override fun onLoopMode(): LiveData<Int> = loopModeLive

    override fun setProgress(progress: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            musicPlayer?.seekTo(
                progress * playList[playPosition.get()].duration / 100,
                MediaPlayer.SEEK_CLOSEST
            )
        } else {
            musicPlayer?.seekTo((progress * playList[playPosition.get()].duration).toInt())
        }
    }

    override fun onMusicPlaying(): LiveData<Music> = currentMusic

    override fun init(music: Music, progress: Long) {
        launch(Dispatchers.Default) {
            mutex.withLock {
                currentMusic.postValue(
                    if (playList.isNotEmpty() && music.title != "NO MUSIC") {
                        val index = playList.indexOf(music)
                        if (index == -1) {
                            playPosition.set(0)
                            getEmptyMusic()
                        } else {
                            playPosition.set(index)
                            initMusicPlayer()
                            playList[playPosition.get()]
                        }
                    } else getEmptyMusic()
                )
            }
        }
    }

    override fun finishMusic() {
        musicPlayer?.apply {
            stop()
            reset()
            release()
            musicPlayer = null
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
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
                if (playPosition.get() <= playList.size - 1) musicBroadCastManager.sendBroadcast(
                    Intent(
                        MUSIC_NEXT
                    )
                )
                else musicBroadCastManager.sendBroadcast(Intent(MUSIC_PAUSE))
            }
        }
    }
}

class MusicBinder(private val iMusic: IMusicService) : Binder(), IMusicService {
    override fun setLoopMode(mode: Int) = iMusic.setLoopMode(mode)
    override fun play(music: Music?) = iMusic.play(music)
    override fun pause() = iMusic.pause()
    override fun next() = iMusic.next()
    override fun previous() = iMusic.previous()
    override fun finishMusic() = iMusic.finishMusic()
    override fun getPlayList(): MutableList<Music> = iMusic.getPlayList()
    override fun setPlayList(list: MutableList<Music>) = iMusic.setPlayList(list)
    override fun getPlayState(): Boolean = iMusic.getPlayState()
    override fun onProgress(): LiveData<Long> = iMusic.onProgress()
    override fun onPlayState(): LiveData<Boolean> = iMusic.onPlayState()
    override fun onLoopMode(): LiveData<Int> = iMusic.onLoopMode()
    override fun setProgress(progress: Long) = iMusic.setProgress(progress)
    override fun onMusicPlaying(): LiveData<Music> = iMusic.onMusicPlaying()
    override fun init(music: Music, progress: Long) = iMusic.init(music, progress)
}

internal abstract class MusicServiceReceiver(private val iMusic: IMusicService) : MusicReceiver() {

    override fun play() {
        iMusic.getPlayState().let {
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
    fun getPlayList(): MutableList<Music>
    fun setPlayList(list: MutableList<Music>)
    fun getPlayState(): Boolean
    fun onProgress(): LiveData<Long>
    fun onPlayState(): LiveData<Boolean>
    fun onLoopMode(): LiveData<Int>
    fun setProgress(progress: Long)
    fun onMusicPlaying(): LiveData<Music>
    fun init(music: Music, progress: Long)
}