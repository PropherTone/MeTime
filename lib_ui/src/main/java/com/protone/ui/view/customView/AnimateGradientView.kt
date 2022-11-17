package com.protone.ui.view.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.protone.ui.R
import kotlinx.coroutines.*
import kotlin.math.pow
import kotlin.math.sqrt

class AnimateGradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var scope: CoroutineScope? = null

    private var linearGradient: LinearGradient? = null
    private var paint = Paint()

    private var scroll = 0f
    private var colors = intArrayOf(
        resources.getColor(R.color.blue_5, null),
        resources.getColor(R.color.blue_1, null)
    )
    private var steep = 50f
    private var millis: Long = 50
    private var v = 0

    private var angel = -1f

    init {
        context.theme
            .obtainStyledAttributes(
                attrs,
                R.styleable.GradientRelative, 0, 0
            ).apply {
                angel = getFloat(R.styleable.GradientRelative_y1, -1f)
                steep = getFloat(R.styleable.GradientRelative_GradientSteep, 40f)
                colors[0] = getColor(
                    R.styleable.GradientRelative_color1,
                    colors[0]
                )
                colors[1] = getColor(
                    R.styleable.GradientRelative_color2,
                    colors[1]
                )
                recycle()
            }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (angel == -1f) {
            angel = measuredHeight.toFloat()
        }
        linearGradient = LinearGradient(
            0f, 0f,
            measuredWidth.toFloat(), angel,
            colors, null, Shader.TileMode.MIRROR
        )
        v = (measuredWidth.toDouble().pow(2.0) + angel.toDouble().pow(2.0)).toInt()
        //millis = 2000 / x
        //x = v / steep
        millis = (2000 / (sqrt(v.toDouble()) / steep)).toLong()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.shader = linearGradient
        canvas.drawRect(0f, 0f, right.toFloat(), bottom.toFloat(), paint)
        canvas.drawPaint(paint)
    }

    fun start() {
        if (scope != null) return
        scope = MainScope()
        scope?.launch(Dispatchers.IO) {
            while (isActive) {
                scroll += steep
                if (scroll >= v) {
                    scroll = 0f
                }
                linearGradient = LinearGradient(
                    scroll, 0f,
                    measuredWidth + scroll, angel,
                    colors, null, Shader.TileMode.MIRROR
                )
                withContext(Dispatchers.Main) {
                    invalidate()
                }
                delay(millis)
            }
        }?.start()
    }

    fun stop() {
        scope?.cancel()
        scope = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }
}