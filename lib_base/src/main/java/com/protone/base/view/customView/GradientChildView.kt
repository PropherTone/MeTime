package com.protone.base.view.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.protone.base.R
import kotlinx.coroutines.*

class GradientChildView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gradientFore: Bitmap? = null
    private var scope: CoroutineScope? = null
    private var animateX = 0f
    private var startPosition = 0f
    private var color = Color.TRANSPARENT
    private var duration = 200

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.GradientView, 0, 0).apply {
            duration = getInt(R.styleable.GradientView_GradientView_duration, duration)
            color = getColor(R.styleable.GradientView_GradientView_color, color)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradientFore = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)?.let {
            val canvasBack = Canvas(it)
            canvasBack.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().also { paint ->
                paint.isAntiAlias = true
                paint.shader = LinearGradient(
                    0f,
                    0f,
                    w.toFloat(),
                    0f,
                    intArrayOf(
                        Color.TRANSPARENT,
                        color,
                        Color.TRANSPARENT
                    ),
                    null,
                    Shader.TileMode.MIRROR
                )
            })
            it
        }
        startPosition = w.toFloat()
        animateX = -startPosition
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        gradientFore?.let { canvas?.drawBitmap(it, animateX, 0f, null) }
    }

    fun startAnimate() {
        if (scope != null) return
        scope = MainScope()
        scope?.launch(Dispatchers.IO) {
            while (isActive) {
                if (animateX > width) {
                    animateX = -startPosition
                }
                animateX+= duration / 1000f
                withContext(Dispatchers.Main) {
                    invalidate()
                }
            }
        }?.start()
    }

    fun stopAnimate() {
        scope?.cancel()
        scope = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimate()
    }
}