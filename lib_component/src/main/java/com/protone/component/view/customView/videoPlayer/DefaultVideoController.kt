package com.protone.component.view.customView.videoPlayer

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.protone.common.baseType.toStringMinuteTime
import com.protone.common.context.newLayoutInflater
import com.protone.component.databinding.VideoControllerLayoutBinding
import com.protone.component.view.customView.ColorfulProgressBar

class DefaultVideoController(context: Context) : VideoBaseController() {

    private val controller by lazy {
        VideoControllerLayoutBinding.inflate(context.newLayoutInflater).apply {
            centerStart.setOnClickListener {
                setPlayState(true)
                doPlay()
                centerStart.isGone = true
            }
            progressBar.progressListener = ColorfulProgressBar.Progress {
                videoPlayer?.seekTo(it)
            }
        }
    }

    init {
        defaultInit()
    }

    override val startBtn: ImageView
        get() = controller.start
    override val preBtn: ImageView
        get() = controller.pre
    override val nextBtn: ImageView
        get() = controller.next
    override val controllerVisibleGroup: View
        get() = controller.cover

    override fun getControllerView(): View = controller.root

    override fun reset() {
        super.reset()
        controller.centerStart.isGone = false
        controllerVisibleGroup.isVisible = false
        controller.progressBar.barSeekTo(0L)
    }

    override fun reverseControllerVisible() {
        if (controller.centerStart.isVisible) return
        super.reverseControllerVisible()
    }

    override fun setDuration(duration: Long) {
        controller.progressBar.barDuration = duration
        controller.totalTime.text = duration.toStringMinuteTime()
    }

    override fun seekTo(progress: Long) {
        controller.progressBar.barSeekTo(progress)
        controller.currentTime.text = progress.toStringMinuteTime()
    }

    override fun setTitle(title: String) {
        controller.title.text = title
    }
}