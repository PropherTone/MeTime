package com.protone.common.media.musicPlayer

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import com.protone.common.entity.Music
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.timerTask

class MusicPlayer : BaseMusicPlayer() {

    private var mediaPlayer: MediaPlayer? = null
    private var weakContext: WeakReference<Context>? = null

    private var timer: Timer? = null

    private fun MediaPlayer.startTimer() {
        if (timer == null) timer = Timer()
        timer?.schedule(timerTask {
            try {
                if (isPlaying) onProgress?.invoke(currentPosition.toLong())
            } catch (ignored: Exception) {
            }
        }, 0, this@MusicPlayer.mProgressCallbackDuration)
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    override fun init(context: Context) {
        weakContext = WeakReference(context)
    }

    override fun prepare(music: Music) {
        if (weakContext == null) return
        val context = weakContext?.get() ?: return
        mediaPlayer = MediaPlayer.create(context, music.uri).apply {
            this@MusicPlayer.duration = music.duration
            setOnCompletionListener {
                onCompletion?.invoke()
            }
        }
    }

    override fun play(music: Music?) {
        if (weakContext == null) return
        val context = weakContext?.get() ?: return
        mediaPlayer = if (music != null) {
            if (mediaPlayer != null) release()
            MediaPlayer.create(context, music.uri).apply {
                this@MusicPlayer.duration = music.duration
                setOnCompletionListener {
                    onCompletion?.invoke()
                }
            }
        } else mediaPlayer
        mediaPlayer?.apply {
            start()
            onStart?.invoke()
            startTimer()
        }
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            cancelTimer()
            onPause?.invoke()
        }
    }

    override fun release() {
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    override fun seekTo(progress: Long) {
        mediaPlayer?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                seekTo(
                    progress * this@MusicPlayer.duration / 100,
                    MediaPlayer.SEEK_CLOSEST
                )
            } else {
                seekTo((progress * this@MusicPlayer.duration).toInt())
            }
        }
    }
}