package com.protone.component.view.customView.imageRegionLoadingView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.Log
import com.protone.common.utils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class RegionDecoder(
    private val coroutineScope: CoroutineScope,
    private val onDecoderListener: OnDecoderListener
) {

    interface OnDecoderListener {
        fun onResourceReady(resource: Bitmap)
    }

    private var bitmapRegionDecoder: BitmapRegionDecoder? = null

    private val bitmapPaint: Paint = Paint().apply { flags = Paint.FILTER_BITMAP_FLAG }
    private var fullImage: Bitmap? = null

    private val imageOriginalRect = Rect(0, 0, 0, 0)
    private val disPlayRect = Rect(0, 0, 0, 0)
    private val sampleRect = Rect(0, 0, 0, 0)

    private var srcScaledW = 0f
    private var srcScaledH = 0f

    fun setImageResource(path: String, w: Int, h: Int) {
        coroutineScope.launch {
            release()
            val file = File(path)
            if (!file.isFile) return@launch
            if (file.isDirectory) return@launch
            file.inputStream().initDecoder(w, h)
        }
    }

    @SuppressLint("Recycle")
    fun setImageResource(context: Context, uri: Uri, w: Int, h: Int) {
        coroutineScope.launch {
            release()
            context.contentResolver?.openInputStream(uri)?.initDecoder(w, h)
        }
    }

    fun drawOriginImage(canvas: Canvas?) {
        fullImage?.let {
            canvas?.drawBitmap(it, null, imageOriginalRect, bitmapPaint)
        }
    }

    private val options by lazy { BitmapFactory.Options() }

    fun drawScaled(
        scaleValue: Float,
        localRect: Rect,
        canvas: Canvas?
    ) {
        if (scaleValue <= 1f) return
        bitmapRegionDecoder?.let { decoder ->
            val scaledW = (imageOriginalRect.right * scaleValue).toInt()
            val scaledH = (imageOriginalRect.bottom * scaleValue).toInt()

            options.inSampleSize = calculateInSampleSize(
                decoder.width,
                decoder.height,
                scaledW,
                scaledH
            )
//            if (options.inSampleSize <= 1) return

            imageOriginalRect.apply {

                disPlayRect.left = (localRect.left / scaleValue).toInt()
                disPlayRect.top = (localRect.top / scaleValue).toInt()
                disPlayRect.right = (localRect.right / scaleValue).toInt()
                disPlayRect.bottom = (localRect.bottom / scaleValue).toInt()

                val l = disPlayRect.left - left
                val t = disPlayRect.top - top
                val r = disPlayRect.right - right
                val b = disPlayRect.bottom - bottom
                sampleRect.set(
                    (l * srcScaledW).toInt(),
                    (t * srcScaledH).toInt(),
                    (r * srcScaledW).toInt(),
                    (b * srcScaledH).toInt()
                )
                if (disPlayRect.left < imageOriginalRect.left) {
                    sampleRect.left = 0
                    sampleRect.right = decoder.width
                    disPlayRect.left = imageOriginalRect.left
                }
                if (disPlayRect.right > imageOriginalRect.right) {
                    sampleRect.right = (decoder.width - disPlayRect.left * srcScaledW).toInt()
                    disPlayRect.right = imageOriginalRect.right
                }
                if (disPlayRect.top < imageOriginalRect.top) {
                    sampleRect.top = 0
                    sampleRect.bottom = decoder.height
                    disPlayRect.top = imageOriginalRect.top
                }
                if (disPlayRect.bottom > imageOriginalRect.bottom) {
                    sampleRect.bottom = (decoder.height - disPlayRect.top * srcScaledW).toInt()
                    disPlayRect.bottom = imageOriginalRect.bottom
                }
            }

            //draw test rect
            canvas?.drawRect(disPlayRect, Paint().apply {
                color = Color.RED
                strokeWidth = 5f
                style = Paint.Style.STROKE
                isAntiAlias = true
                isDither = true
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            })

            try {
                decoder.decodeRegion(sampleRect, options)
            } catch (_: IllegalArgumentException) {
                null
            }?.let {
                canvas?.drawBitmap(it, null, disPlayRect, bitmapPaint)
            }
        }

    }

    fun getImageMarginTop() = imageOriginalRect.top

    fun getImageMarginBottom() = imageOriginalRect.top

    fun getImageMarginLeft() = imageOriginalRect.left

    fun getImageMarginRight() = imageOriginalRect.left

    fun release() {
        if (fullImage?.isRecycled == true) {
            fullImage?.recycle()
            fullImage = null
        }
        bitmapRegionDecoder?.recycle()
        bitmapRegionDecoder = null
    }

    private suspend fun InputStream.initDecoder(viewWidth: Int, viewHeight: Int) {
        bitmapRegionDecoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BitmapRegionDecoder.newInstance(this)
        } else {
            @Suppress("DEPRECATION")
            BitmapRegionDecoder.newInstance(this, false)
        }
        bitmapRegionDecoder?.let {
            var width = viewWidth
            var height = viewHeight
            when {
                viewWidth == 0 && viewHeight == 0 -> {
                    width = it.width
                    height = it.height
                    imageOriginalRect.right = width
                    imageOriginalRect.bottom = height
                }
                viewWidth == 0 -> {
                    val mix = height / it.height.toFloat()
                    width = (it.width * mix).toInt()
                    imageOriginalRect.right = width
                    imageOriginalRect.bottom = height
                }
                viewHeight == 0 -> {
                    val mix = width / it.width.toFloat()
                    height = (it.height * mix).toInt()
                    imageOriginalRect.right = width
                    imageOriginalRect.bottom = height
                }
                else -> {
                    if (viewHeight >= viewWidth) {
                        val widthCompare = viewWidth / it.width.toFloat()
                        val rectHeight = (it.height * widthCompare).toInt()
                        if (rectHeight > viewHeight) {
                            val heightCompare = viewHeight / it.height.toFloat()
                            val rectWidth = (it.width * heightCompare).toInt()
                            imageOriginalRect.left = (viewWidth / 2) - (rectWidth / 2)
                            imageOriginalRect.right = rectWidth + imageOriginalRect.left
                            imageOriginalRect.top = 0
                            imageOriginalRect.bottom = viewHeight
                            width = rectWidth
                            height = viewHeight
                        } else {
                            imageOriginalRect.left = 0
                            imageOriginalRect.right = viewWidth
                            imageOriginalRect.top = (viewHeight / 2) - (rectHeight / 2)
                            imageOriginalRect.bottom = rectHeight + imageOriginalRect.top
                            width = viewWidth
                            height = rectHeight
                        }
                    } else {
                        val heightCompare = viewHeight / it.height.toFloat()
                        val rectWidth = (it.width * heightCompare).toInt()
                        if (rectWidth > viewWidth) {
                            val widthCompare = viewWidth / it.width.toFloat()
                            val rectHeight = (it.height * widthCompare).toInt()
                            imageOriginalRect.left = 0
                            imageOriginalRect.right = viewWidth
                            imageOriginalRect.top = (viewHeight / 2) - (rectHeight / 2)
                            imageOriginalRect.bottom = rectHeight + imageOriginalRect.top
                            width = viewWidth
                            height = rectHeight
                        } else {
                            imageOriginalRect.left = (viewWidth / 2) - (rectWidth / 2)
                            imageOriginalRect.right = rectWidth + imageOriginalRect.left
                            imageOriginalRect.top = 0
                            imageOriginalRect.bottom = viewHeight
                            width = rectWidth
                            height = viewHeight
                        }
                    }
                }
            }
            srcScaledW = it.width / viewWidth.toFloat()
            srcScaledH = it.height / viewHeight.toFloat()
            it.decodeRegion(Rect(0, 0, width, height), BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(it.width, it.height, width, height)
            })
        }?.also {
            fullImage = it
            this.close()
            withContext(Dispatchers.Main) {
                onDecoderListener.onResourceReady(it)
            }
        }
    }

    //采样率算法
    private fun calculateInSampleSize(
        outWidth: Int,
        outHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        var inSampleSize = 1
        if (outHeight > reqHeight || outWidth > reqWidth) {
            val halfHeight = outHeight / 2
            val halfWidth = outWidth / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            try {
                while (halfHeight / inSampleSize >= reqHeight
                    || halfWidth / inSampleSize >= reqWidth
                ) {
                    inSampleSize *= 2
                }
            } catch (e: ArithmeticException) {
                Log.e(TAG, "half$halfHeight,$halfWidth,inSampleSize$inSampleSize")
                Log.e(TAG, "reqHeight$reqHeight,reqWidth$reqWidth")
            }
        }
        return inSampleSize
    }
}