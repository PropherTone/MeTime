package com.protone.component.view.customView.blurView

import android.graphics.*
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible

abstract class BaseBlurFactory(protected val blurEngine: BlurEngine) : IBlurTool, IBlurConfig {

    companion object {
        const val TAG = "BlurFactory"
    }

    protected val scaleFactory = ScaleFactory()
    protected var decorCanvas: BlurCanvas = BlurCanvas()
    protected var decorBitmap: Bitmap? = null

    protected var canMove = false
    protected var isResized = false

    protected var maskColor: Int = Color.TRANSPARENT
        private set
    protected var xfMode: PorterDuff.Mode = PorterDuff.Mode.ADD
        private set

    protected fun transformCanvas() {
        scaleFactory.apply {
            decorCanvas.save()
            decorCanvas.translate(leftScaled, rightScaled)
            decorCanvas.scale(1 / wScaled, 1 / hScaled)
        }
    }

    protected fun makeCanvas(w: Int, h: Int, config: Bitmap.Config) {
        decorBitmap = Bitmap.createBitmap(w, h, config)
        decorCanvas.setBitmap(decorBitmap)
    }

    override fun setMaskXfMode(mode: PorterDuff.Mode) {
        this.xfMode = mode
    }

    override fun setMaskColor(@ColorInt color: Int) {
        this.maskColor = color
    }

    override fun setBlurRadius(radius: Float) {
        blurEngine.setRadius(radius)
    }

    override fun setWillMove(willMove: Boolean) {
        if (canMove != willMove) {
            if (willMove && isResized) decorCanvas.restore()
            else transformCanvas()
        }
        this.canMove = willMove
    }

    inner class BlurCanvas : Canvas()
}

class DefaultBlurController(private val root: ViewGroup, blurEngine: BlurEngine) :
    BaseBlurFactory(blurEngine) {

    private var blurView: SBlurView? = null

    private val bitmapPaint: Paint = Paint().apply {
        flags = Paint.FILTER_BITMAP_FLAG
    }

    private var isInit = false

    override fun drawDecor() {
        if (!isResized) return
        root.apply {
            if (width <= 0 || height <= 0) return
            if (canMove) {
                decorCanvas.save()
                calculateSize(true)
                draw(decorCanvas)
                decorCanvas.restore()
            } else {
                draw(decorCanvas)
            }
        }
    }

    override fun blur(): Boolean {
        if (!isInit) return false
        if (!root.isVisible && blurView?.isVisible == false) return true
        drawDecor()
        decorBitmap = decorBitmap?.apply { blurEngine.blur(this) }
        return true
    }

    private fun calculateSize(changeWH: Boolean = false) {
        isResized = false
        blurView?.apply {
            scaleFactory.apply {
                if (!changeWH) {
                    wScaled = (width / (decorBitmap?.width ?: width).toFloat())
                    hScaled = (height / (decorBitmap?.height ?: height).toFloat())
                }
                val rootLocation = intArrayOf(0, 0)
                val viewLocation = intArrayOf(0, 0)
                root.getLocationOnScreen(rootLocation)
                getLocationOnScreen(viewLocation)
                val opsX = viewLocation[0] - rootLocation[0]
                val opsY = viewLocation[1] - rootLocation[1]
                leftScaled = -opsX / wScaled
                rightScaled = -opsY / hScaled
            }
            transformCanvas()
        }
        isResized = true
    }

    override fun drawBlurred(canvas: Canvas?): Boolean {
        if (!isInit) return true
        canvas?.apply {
            if (this is BlurCanvas) {
                return false
            }
            save()
            scale(scaleFactory.wScaled, scaleFactory.hScaled)
            decorBitmap?.apply { drawBitmap(this, 0f, 0f, bitmapPaint) }
            restore()
            drawMask(canvas)
        }
        return true
    }

    override fun drawMask(canvas: Canvas?) {
        if (maskColor != Color.TRANSPARENT) {
            canvas?.drawColor(maskColor, xfMode)
        }
    }

    override fun setBlurView(view: SBlurView) {
        isInit = false
        isResized = false
        view.apply {
            blurView = this
            if (width == 0 || height == 0) {
                setWillNotDraw(true)
                return
            }
            init(width, height)
            if (canMove) decorCanvas.restore()
        }
    }

    private fun init(w: Int, h: Int) {
        if (w == 0 || h == 0) {
            Log.e(TAG, "The view's width or height can't be zero.")
            return
        }
        blurView?.setWillNotDraw(false)
        val scaleFactor = blurEngine.getScaleFactor(w)
        makeCanvas(
            (w / scaleFactor).toInt(),
            (h / scaleFactor).toInt(),
            blurEngine.getBitmapConfig()
        )
        calculateSize()
        isInit = true
    }

    override fun release() {
        isInit = false
        blurView?.setWillNotDraw(true)
        decorBitmap?.recycle()
    }

}

class EmptyIBlurTool(blurEngine: BlurEngine = DefaultBlurEngine()) : BaseBlurFactory(blurEngine) {
    override fun drawDecor() = Unit
    override fun blur() = false
    override fun drawBlurred(canvas: Canvas?) = true
    override fun drawMask(canvas: Canvas?) = Unit
    override fun setMaskXfMode(mode: PorterDuff.Mode) = Unit
    override fun setBlurView(view: SBlurView) = Unit
    override fun setMaskColor(color: Int) = Unit
    override fun setBlurRadius(radius: Float) = Unit
    override fun release() = Unit
}