package com.protone.component.view.customView.videoPlayer

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import com.protone.common.utils.displayUtils.imageLoader.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.protone.common.utils.TAG
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.component.view.customView.video.AutoFitTextureView
import kotlin.math.roundToInt

class VideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var mediaPlayer: MediaPlayer? = null
    private var textureView: AutoFitTextureView? = null
    private val previewCover: ImageView
    private var playSurface: Surface? = null

    private var path: String? = null
    private var uriPath: Uri? = null
    private var doPlaying = false
    private var isPrepared = false
    private var isInitialized = false

    private val frameCallBack = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (doPlaying) play()
            else mediaPlayer?.let { controller?.seekTo(it.currentPosition.toLong()) }
            Choreographer.getInstance().postFrameCallbackDelayed(this, 1000)
        }
    }

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
            setOnClickListener {
                value?.reverseControllerVisible()
            }
            value?.setVideoPlay(object : VideoPlayer {
                override fun play() {
                    if (mediaPlayer?.isPlaying == true) return
                    this@VideoPlayerView.play()
                }

                override fun pause() {
                    runCatching {
                        if (mediaPlayer?.isPlaying == false) return
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

    init {
        initTextureView()
        addView(
            ImageView(context).also {
                it.scaleType = ImageView.ScaleType.CENTER_CROP
                previewCover = it
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    fun setPath(path: String, loadPreview: Boolean = true, autoAdjustToFill: Boolean = false) {
        releasePlayer()
        mediaPlayer = MediaPlayer().also {
            it.setDataSource(path)
            this.path = path
            this.uriPath = null
            it.initPlayer()
        }
        if (loadPreview) loadPreview(autoAdjustToFill)
    }

    fun setPath(uri: Uri, loadPreview: Boolean = true, autoAdjustToFill: Boolean = false) {
        releasePlayer()
        mediaPlayer = MediaPlayer().also {
            it.setDataSource(context, uri)
            this.uriPath = uri
            this.path = null
            it.initPlayer()
        }
        if (loadPreview) loadPreview(autoAdjustToFill)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow: ")
        releasePlayer()
        playSurface?.release()
        playSurface = null
        textureView = null
    }

    private fun loadPreview(autoAdjustToFill: Boolean) {
        (uriPath?.let { Image.load(it) } ?: path?.let { Image.load(it) })
            ?.with(context)?.also {
                if (autoAdjustToFill) it.setInterceptor(object : RequestInterceptor() {
                    override fun onLoadSuccess(result: LoadSuccessResult) {
                        result.resource?.apply {
                            val mix = this.intrinsicWidth.toFloat().let { w ->
                                this@VideoPlayerView.width / w
                            }
                            val heightSpan = (this.intrinsicHeight * mix).roundToInt()
                            this@VideoPlayerView.updateLayoutParams {
                                this.height = heightSpan
                            }
                        }
                    }
                })
            }?.into(previewCover)
    }

    private fun initTextureView() {
        textureView = AutoFitTextureView(context).also {
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
            addView(
                it, 0,
                LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
        }
    }

    private fun play() {
        runCatching {
            doPlaying = if (isPrepared && isInitialized) {
                mediaPlayer?.start()
                previewCover.isGone = true
                false
            } else true
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
            }
            setOnPreparedListener {
                isPrepared = true
                controller?.seekTo(0)
                it?.let { controller?.setDuration(it.duration.toLong()) }
            }
            setOnCompletionListener {
                isPrepared = false
                reset()
                previewCover.isGone = false
                controller?.reset()
                uriPath?.let { setDataSource(context, it) }
                path?.let { setDataSource(it) }
                prepareAsync()
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