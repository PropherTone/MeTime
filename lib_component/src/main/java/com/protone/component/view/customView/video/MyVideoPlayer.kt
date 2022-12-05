package com.protone.component.view.customView.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.*
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.protone.common.utils.isInDebug
import com.protone.component.view.customView.ColorfulProgressBar

class MyVideoPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr),
    TextureView.SurfaceTextureListener,
    MediaPlayer.OnVideoSizeChangedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    private var isPrepared: Boolean = false
    private lateinit var path: Uri

    private val videoController: MyVideoController by lazy { MyVideoController(context) }

    var title: String = ""
        set(value) {
            videoController.title = value
            field = value
        }

    val isPlaying get() = videoController.isPlaying

    fun setFullScreen(listener: () -> Unit) {
        videoController.fullScreen(listener)
    }

    private var onClick: () -> Unit = {}

    fun setOnClickEvent(block: () -> Unit) {
        onClick = block
    }

    private var mediaPlayer: MediaPlayer? = null
    private val textureView: MyTextureView? by lazy { MyTextureView(context) }

    private var surface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
        set(value) {
            field = value
            surface = Surface(value)
        }

    private val choreographer = Choreographer.getInstance()

    private val frameCallBack = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            mediaPlayer?.let { videoController.seekTo(it.currentPosition.toLong()) }
            choreographer.postFrameCallbackDelayed(this, 1000)
        }
    }

    init {
        videoController.apply {
            playVideo { play() }
            pauseVideo { pause() }
            setProgressListener(object : ColorfulProgressBar.Progress {
                override fun getProgress(position: Long) {
                    videoSeekTo(position)
                }
            })
        }
        removeView(videoController)
        addView(
            videoController,
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        setOnClickListener {
            onClick()
            if (videoController.isPlaying) {
                videoController.isVisible = !videoController.isVisible
            }
        }
    }

    private fun initPlayer() {
        initVideoPlayer()
        initTextureView()
    }

    private fun initVideoPlayer() {
        try {
            release()
            mediaPlayer = MediaPlayer()
        } catch (e: Exception) {
            if (isInDebug()) e.printStackTrace()
        }
    }

    private fun initTextureView() {
        textureView?.surfaceTextureListener = this
        removeView(textureView)
        addView(
            textureView, 0, LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
    }

    fun playVideo(func: () -> Unit) {
        videoController.playVideo(func)
    }

    fun setVideoPath(path: Uri) {
        this.path = path
        loadCover(path)
        initPlayer()
    }

    private fun startProgress() {
        choreographer.postFrameCallback(frameCallBack)
    }

    fun play() {
        try {
            if (mediaPlayer?.isPlaying == false && isPrepared) {
                mediaPlayer?.start()
                videoController.onStart()
                startProgress()
            }
        } catch (e: IllegalStateException) {
            if (isInDebug()) e.printStackTrace()
        }
    }

    private fun loadCover(path: String) {
        videoController.loadCover(path)
    }

    private fun loadCover(path: Uri) {
        videoController.loadCover(path)
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                choreographer.removeFrameCallback(frameCallBack)
            }
        } catch (e: IllegalStateException) {
            if (isInDebug()) e.printStackTrace()
        }

    }

    fun release() {
        try {
            choreographer.removeFrameCallback(frameCallBack)
            mediaPlayer?.apply {
                stop()
                videoController.complete()
                release()
                isPrepared = false
            }
            mediaPlayer = null
        } catch (e: IllegalStateException) {
            if (isInDebug()) e.printStackTrace()
        }
    }

    private fun videoSeekTo(position: Long) {
        try {
            mediaPlayer?.duration?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer?.seekTo(
                        (position.toFloat() / 100 * it).toLong(),
                        MediaPlayer.SEEK_CLOSEST
                    )
                } else {
                    mediaPlayer?.seekTo((position.toFloat() / 100 * it).toInt())
                }
            }
        } catch (e: IllegalStateException) {
            if (isInDebug()) e.printStackTrace()
        }
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        try {
            if (surfaceTexture == null) {
                surfaceTexture = p0
                mediaPlayer?.apply {
                    setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                            .build()
                    )
                    setDataSource(context, path)
                    setSurface(surface)
                    prepareAsync()
                    setOnVideoSizeChangedListener(this@MyVideoPlayer)
                    setOnCompletionListener(this@MyVideoPlayer)
                    setOnPreparedListener(this@MyVideoPlayer)
                }
            } else {
                surfaceTexture?.apply {
                    textureView?.setSurfaceTexture(this)
                }
            }
        } catch (e: Exception) {
            if (isInDebug()) e.printStackTrace()
        }
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return textureView == null
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}

    override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {
        textureView?.adaptVideoSize(p1, p2)
        textureView?.measuredHeight?.let {
            textureView?.measuredWidth?.let { it1 -> measure(it1, it) }
        }
    }

    private var onComplete: (() -> Unit)? = null

    fun doOnCompletion(block: () -> Unit) {
        onComplete = block
    }

    override fun onCompletion(p0: MediaPlayer?) {
        release()
        onComplete?.invoke()
    }

    override fun onPrepared(p0: MediaPlayer?) {
        isPrepared = true
        videoController.seekTo(0)
        p0?.let { videoController.setVideoDuration(it.duration.toLong()) }
    }
}
