package com.protone.component.view.customView.videoPlayer

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.R

abstract class VideoBaseController(private val context: Context) {

    var isPlaying = false
        set(value) {
            if (value == field) return
            startBtn.setImageDrawable(
                if (!value) R.drawable.ic_round_play_arrow_24_white.getDrawable()
                else R.drawable.ic_round_pause_24_white.getDrawable()
            )
            if (previewCover?.isVisible == true) previewCover?.isGone = true
            videoPlayer?.let { if (value) it.play() else it.pause() }
            controllerVisibleGroup.isVisible = !controllerVisibleGroup.isVisible
            field = value
        }

    protected var videoPlayer: VideoPlayer? = null

    protected abstract val startBtn: ImageView
    protected abstract val preBtn: ImageView?
    protected abstract val nextBtn: ImageView?
    protected abstract val previewCover: ImageView?
    protected abstract val controllerVisibleGroup: View
    abstract fun getControllerView(): View
    abstract fun seekTo(progress: Long)

    open fun setDuration(duration: Long) = Unit

    open fun reset() {
        isPlaying = false
        previewCover?.isGone = false
    }

    protected fun defaultInit() {
        startBtn.setOnClickListener {
            isPlaying = !isPlaying
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

    fun loadPreview(path: String) {
        previewCover?.let { Image.load(path).with(context).into(it) }
    }

    fun loadPreview(path: Uri) {
        previewCover?.let { Image.load(path).with(context).into(it) }
    }

}