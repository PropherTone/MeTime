package com.protone.component.view.customView.videoPlayer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.component.R

abstract class VideoBaseController {

    enum class PlayState {
        PLAYING,
        PAUSE,
        STOP
    }

    private val mHandler by lazy {
        Handler(Looper.getMainLooper()) {
            controllerVisibleGroup.isVisible = false
            false
        }
    }

    var state = PlayState.STOP
        protected set

    protected var videoPlayer: VideoPlayer? = null

    protected abstract val startBtn: ImageView
    protected abstract val preBtn: ImageView?
    protected abstract val nextBtn: ImageView?
    protected abstract val controllerVisibleGroup: View
    abstract fun getControllerView(): View
    abstract fun setTitle(title: String)
    open fun setDuration(duration: Long) = Unit

    open fun seekTo(progress: Long){
        if(state != PlayState.PLAYING) return
    }

    open fun reset() {
        mHandler.removeCallbacksAndMessages(null)
        setPlayState(PlayState.STOP)
    }

    open fun reverseControllerVisible() {
        controllerVisibleGroup.isVisible = !controllerVisibleGroup.isVisible.also {
            if (it) doHideCountdown()
        }
    }

    private fun doHideCountdown() {
        mHandler.removeCallbacksAndMessages(null)
        mHandler.sendEmptyMessageDelayed(0, 3000L)
    }

    protected fun defaultInit() {
        startBtn.setOnClickListener {
            setPlayState(if (state == PlayState.PLAYING) PlayState.PAUSE else PlayState.PLAYING)
            doPlay()
        }
        preBtn?.setOnClickListener {
            videoPlayer?.onPre()
        }
        nextBtn?.setOnClickListener {
            videoPlayer?.onNext()
        }
    }

    internal fun setVideoPlay(videoPlayer: VideoPlayer) {
        this.videoPlayer = videoPlayer
    }

    internal fun doPlay() {
        videoPlayer?.let {
            when (state) {
                PlayState.PLAYING -> it.play()
                PlayState.PAUSE -> it.pause()
                PlayState.STOP -> it.stop()
            }
            controllerVisibleGroup.isVisible = true
            doHideCountdown()
        }
    }

    internal fun setPlayState(isPlaying: PlayState) {
        if (isPlaying == this.state) return
        this.state = isPlaying
        startBtn.setImageDrawable(
            if (isPlaying != PlayState.PLAYING) R.drawable.ic_round_play_arrow_24_white.getDrawable()
            else R.drawable.ic_round_pause_24_white.getDrawable()
        )
    }

    fun play() {
        if (state == PlayState.PLAYING) return
        setPlayState(PlayState.PLAYING)
        doPlay()
    }

    fun pause() {
        if (state != PlayState.PLAYING) return
        setPlayState(PlayState.PAUSE)
        doPlay()
    }

    fun stop() {
        if (state != PlayState.PLAYING) return
        setPlayState(PlayState.STOP)
        doPlay()
    }

}