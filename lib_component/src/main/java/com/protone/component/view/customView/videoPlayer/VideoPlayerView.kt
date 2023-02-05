package com.protone.component.view.customView.videoPlayer

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
import com.protone.component.view.customView.video.AutoFitTextureView

class VideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    var controller: VideoBaseController? = null
        set(value) {
            removeView(field?.getControllerView())
            addView(
                value?.getControllerView(),
                LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            value?.setVideoPlay(object : VideoPlayer {
                override fun play() {
                    this@VideoPlayerView.play()
                }

                override fun pause() {
                    runCatching {
                        mediaPlayer?.pause()
                        Choreographer.getInstance().removeFrameCallback(frameCallBack)
                    }
                }

                override fun stop() {
                    runCatching {
                        mediaPlayer?.stop()
                        Choreographer.getInstance().removeFrameCallback(frameCallBack)
                    }
                }

                override fun onPre() = Unit
                override fun onNext() = Unit

                override fun seekTo(progress: Long) {
                    runCatching {
                        mediaPlayer?.duration?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mediaPlayer?.seekTo(
                                    (progress.toFloat() / 100 * it).toLong(),
                                    MediaPlayer.SEEK_CLOSEST
                                )
                            } else mediaPlayer?.seekTo((progress.toFloat() / 100 * it).toInt())
                        }
                    }
                }

            })
            field = value
        }

    private var mediaPlayer: MediaPlayer? = null
    private var textureView: AutoFitTextureView? = null
    private var playSurface: Surface? = null

    private var path: String? = null
    private var uriPath: Uri? = null
    private var doPlaying = false
    private var isPrepared = false
    private var isInitialized = false

    private val frameCallBack = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (doPlaying) {
                play()
                Choreographer.getInstance().postFrameCallbackDelayed(this, 1000)
                return
            }
            mediaPlayer?.let { controller?.seekTo(it.currentPosition.toLong()) }
            Choreographer.getInstance().postFrameCallbackDelayed(this, 1000)
        }
    }

    init {
        initTextureView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
        playSurface?.release()
        playSurface = null
        textureView = null
    }

    fun setPath(path: String) {
        releasePlayer()
        mediaPlayer = MediaPlayer().also {
            it.setDataSource(path)
            this.path = path
            this.uriPath = null
            it.initPlayer()
        }
    }

    fun setPath(uri: Uri) {
        releasePlayer()
        controller?.loadPreview(uri)
        mediaPlayer = MediaPlayer().also {
            it.setDataSource(context, uri)
            this.uriPath = uri
            this.path = null
            it.initPlayer()
        }
    }

    private fun initTextureView() {
        textureView = AutoFitTextureView(context).also {
            addView(
                it,
                LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
            it.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    playSurface = Surface(surface)
                    if (mediaPlayer == null) {
                        path?.let { p -> setPath(p) } ?: uriPath?.let { p -> setPath(p) }
                    } else if (!isInitialized) playSurface?.let { s ->
                        mediaPlayer?.setSurface(s)
                        isInitialized = true
                    }
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) = Unit

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

            }
        }
    }

    private fun play() {
        if (isPrepared && isInitialized) {
            runCatching {
                mediaPlayer?.start()
                doPlaying = false
                Choreographer.getInstance().postFrameCallback(frameCallBack)
            }
        } else {
            doPlaying = true
            Choreographer.getInstance().postFrameCallback(frameCallBack)
        }
    }

    private fun MediaPlayer.initPlayer() {
        runCatching {
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            playSurface?.let { surface ->
                setSurface(surface)
                isInitialized = true
            }
            setOnVideoSizeChangedListener { _, width, height ->
                textureView?.adaptVideoSize(width, height)
//            measure(textureView.measuredWidth, textureView.measuredHeight)
            }
            setOnPreparedListener {
                isPrepared = true
                controller?.seekTo(0)
                it?.let { controller?.setDuration(it.duration.toLong()) }
            }
            setOnCompletionListener {
                reset()
                controller?.reset()
            }
            prepareAsync()
        }
    }

    private fun releasePlayer() {
        runCatching {
            mediaPlayer?.release()
            mediaPlayer = null
            controller?.reset()
        }
    }

}