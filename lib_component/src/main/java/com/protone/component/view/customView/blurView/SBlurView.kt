package com.protone.component.view.customView.blurView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout

open class SBlurView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), IBlurConfig {

    private var blurTool: BaseBlurFactory = EmptyIBlurTool()
        set(value) {
            value.setBlurView(this)
            field = value
        }

    private var maskColor: Int = Color.TRANSPARENT

    fun initBlurTool(blurTool: BaseBlurFactory): BaseBlurFactory {
        this.blurTool = blurTool
        return this.blurTool
    }

    override fun onDrawForeground(canvas: Canvas?) {
        if (maskColor == Color.TRANSPARENT) return
        canvas?.drawColor(maskColor)
        super.onDrawForeground(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        blurTool.drawBlurred(canvas)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blurTool.setBlurView(this)
    }

    fun renderFrame() {
        blurTool.blur()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    fun release(): BaseBlurFactory {
        blurTool.release()
        return blurTool
    }

    fun setForeColor(color: Int) {
        this.maskColor = color
    }

    override fun setMaskXfMode(mode: PorterDuff.Mode) {
        blurTool.setMaskXfMode(mode)
    }

    override fun setMaskColor(@ColorInt color: Int) {
        blurTool.setMaskColor(color)
    }

    override fun setBlurRadius(radius: Float) {
        blurTool.setBlurRadius(radius)
    }

    override fun setWillMove(willMove: Boolean) {
        blurTool.setWillMove(willMove)
    }

}