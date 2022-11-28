package com.protone.component.view.customView.video

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.common.context.newLayoutInflater
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.R
import com.protone.component.databinding.VideoControllerBinding
import com.protone.component.view.customView.ColorfulProgressBar

class MyVideoController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = VideoControllerBinding.inflate(context.newLayoutInflater, this, true)

    private var playVideo: (() -> Unit)? = null
    private var pauseVideo: (() -> Unit)? = null
    private var fullScreen: (() -> Unit)? = null

    var title: String = ""
        set(value) {
            binding.vTitle.text = value
            field = value
        }

    fun playVideo(func: () -> Unit) {
        playVideo = func
    }

    fun pauseVideo(func: () -> Unit) {
        pauseVideo = func
    }

    fun fullScreen(func: () -> Unit) {
        fullScreen = func
    }

    fun loadCover(path: String) {
        Image.load(path).with(context).into(binding.vVideoCover)
    }

    fun loadCover(path: Uri) {
        Image.load(path).with(context).into(binding.vVideoCover)
    }

    private var isPlaying = false
        set(value) {
            binding.vStart.setImageDrawable(
                if (!value) R.drawable.ic_baseline_play_arrow_24_white.getDrawable()
                else R.drawable.ic_baseline_pause_24_white.getDrawable()
            )
            binding.vControl.setImageDrawable(
                if (!value) R.drawable.ic_baseline_play_arrow_24_white.getDrawable()
                else R.drawable.ic_baseline_pause_24_white.getDrawable()
            )
            field = value
        }

    init {
        binding.vFull.setOnClickListener {
            fullScreen?.invoke()
        }
        binding.vStart.setOnClickListener {
            if (!isPlaying) {
                playVideo?.invoke()
                binding.vSeekBar.start()
            }
            onPlay()
            isPlaying = true
        }
        binding.vControl.setOnClickListener {
            if (!isPlaying) {
                playVideo?.invoke()
                binding.vSeekBar.start()
            } else {
                pauseVideo?.invoke()
                binding.vSeekBar.stop()
            }
            isPlaying = !isPlaying
        }
    }

    fun onStart() {
        if (isPlaying) return
        onPlay()
        isPlaying = true
    }

    private fun onPlay() {
        binding.vStart.isVisible = false
        binding.vVideoCover.isGone = true
        binding.vContainer.isVisible = true
        isVisible = false
    }

    fun complete() {
        isPlaying = false
        isVisible = true
        binding.vStart.isVisible = true
        binding.vContainer.isVisible = false
        binding.vVideoCover.isGone = false
        binding.vSeekBar.stop()
        binding.vSeekBar.barSeekTo(0)
    }

    fun setVideoDuration(duration: Long) {
        binding.vSeekBar.barDuration = duration
    }

    fun seekTo(duration: Long) = binding.vSeekBar.barSeekTo(duration)

    fun setProgressListener(listener: ColorfulProgressBar.Progress) {
        binding.vSeekBar.progressListener = listener
    }
}

