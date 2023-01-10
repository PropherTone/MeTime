package com.protone.component.view.customView.musicPlayer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withMainContext
import com.protone.common.utils.BitmapCachePool
import com.protone.common.utils.displayUtils.Blur
import com.protone.common.utils.isInDebug
import com.protone.component.R
import com.protone.component.view.customView.ColorfulProgressBar
import com.protone.component.view.customView.SwitchImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

val bitmapCachePool by lazy { BitmapCachePool() }

suspend fun Uri.getBitmap() = bitmapCachePool.get(this)

suspend fun String.getBitmap() = try {
    bitmapCachePool.get(this)
} catch (e: NullPointerException) {
    null
}

abstract class BaseMusicPlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    val baseAlbumDrawable by lazy {
        ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_music_note_24, null)
    }
    val baseCoverDrawable by lazy {
        ResourcesCompat.getDrawable(resources, R.drawable.main_background, null)
    }
    abstract val next: ImageView?
    abstract val control: ImageView
    abstract val previous: ImageView?
    abstract val progress: ColorfulProgressBar?
    abstract val switcher: SwitchImageView
    abstract val coverSwitcher: SwitchImageView
    abstract var looper: ImageView?
    abstract val root: View

    open var duration: Long? = 0L
        set(value) {
            progress?.barDuration = value ?: 0L
            field = value
        }

    var cover: Uri = Uri.EMPTY
        set(value) {
            loadAlbum(value)
            field = value
        }

    var isPlay = false
        set(value) {
            if (value) onPlay() else onPause()
            field = value
        }

    var interceptAlbumCover = false
        set(value) {
            switcher.setImageBitmap(null)
            switcher.setImageBitmap(null)
            field = value
        }

    private var onBlurAlbumCover: ((Bitmap?) -> Unit)? = null
    fun onBlurAlbumCover(block: (Bitmap?) -> Unit) {
        this.onBlurAlbumCover = block
    }

    abstract fun onPlay()
    abstract fun onPause()
    abstract fun setName(name: String)
    abstract fun setDetail(detail: String)

    private fun loadAlbum(albumUri: Uri?) {
        launch {
            val albumBitmap = albumUri?.let { bitmapCachePool.get(it) }
            if (albumBitmap == null) {
                coverSwitcher.setImageDrawable(baseAlbumDrawable)
            } else {
                try {
                    coverSwitcher.setImageBitmap(albumBitmap)
                } catch (e: Exception) {
                    if (isInDebug()) e.printStackTrace()
                    coverSwitcher.setImageDrawable(baseAlbumDrawable)
                }
            }
            loadBlurCover(albumBitmap)
        }
    }

    private suspend fun loadBlurCover(albumBitmap: Bitmap?) {
        withDefaultContext {
            try {
                if (albumBitmap == null) {
                    if (interceptAlbumCover) return@withDefaultContext
                    withMainContext {
                        switcher.setImageDrawable(baseCoverDrawable)
                    }
                    return@withDefaultContext
                }
                val blur = Blur.blur(albumBitmap, radius = 12, sampling = 10)
                if (interceptAlbumCover) {
                    withMainContext {
                        onBlurAlbumCover?.invoke(blur)
                    }
                    return@withDefaultContext
                }
                withMainContext {
                    switcher.setImageBitmap(blur)
                }
            } catch (e: Exception) {
                if (isInDebug()) e.printStackTrace()
                if (!interceptAlbumCover) withMainContext {
                    switcher.setImageDrawable(baseCoverDrawable)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}