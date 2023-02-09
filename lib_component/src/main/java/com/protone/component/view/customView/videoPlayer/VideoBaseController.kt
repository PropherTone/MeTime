package com.protone.component.view.customView.videoPlayer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.component.R

abstract class VideoBaseController {

    private val mHandler by lazy {
        Handler(Looper.getMainLooper()) {
            controllerVisibleGroup.isVisible = false
            false
        }
    }

    var isPlaying = false
        protected set

    protected var videoPlayer: VideoPlayer? = null

    protected abstract val startBtn: ImageView
    protected abstract val preBtn: ImageView?
    protected abstract val nextBtn: ImageView?
    protected abstract val controllerVisibleGroup: View
    abstract fun getControllerView(): View
    abstract fun seekTo(progress: Long)
    abstract fun setTitle(title: String)

    open fun setDuration(duration: Long) = Unit

    open fun reset() {
        mHandler.removeCallbacksAndMessages(null)
        setPlayState(false)
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
            setPlayState(!isPlaying)
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
            if (isPlaying) it.play() else it.pause()
            controllerVisibleGroup.isVisible = true
            doHideCountdown()
        }
    }

    internal fun setPlayState(isPlaying: Boolean) {
        if (isPlaying == this.isPlaying) return
        this.isPlaying = isPlaying
        startBtn.setImageDrawable(
            if (!isPlaying) R.drawable.ic_round_play_arrow_24_white.getDrawable()
            else R.drawable.ic_round_pause_24_white.getDrawable()
        )
    }

    fun play() {
        if (isPlaying) return
        setPlayState(true)
        doPlay()
    }

    fun pause() {
        if (!isPlaying) return
        setPlayState(false)
        doPlay()
    }

}