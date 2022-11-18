package com.protone.component.view.customView

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.protone.common.context.onGlobalLayout

class Bubble(context: Context) {

    enum class BubblePosition {
        BOTTOM,
        TOP,
        LEFT,
        RIGHT
    }

    enum class BubbleGravity {
        START,
        CENTER,
        END
    }

    enum class Location {
        TOP,
        BOTTOM,
        START,
        END
    }

    private val bubbleView = BubbleView(context).also {
        it.alpha = 0f
        it.layoutParams = ViewGroup.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        it.addView(TextView(context).apply {
            this.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(12, 12, 12, 12)
            paint.isAntiAlias = true
        })
        if (context is Activity) {
            context.window.findViewById<ViewGroup>(android.R.id.content).apply {
                addView(it)
            }
        }
    }

    fun setText(msg: CharSequence): Bubble {
        bubbleView.text = msg
        return this
    }

    fun setBubbleRadius(r: Float): Bubble {
        bubbleView.setBubbleRadius(r)
        return this
    }

    private var onMeasured = false

    fun showBubble(targetView: View?, location: Location = Location.TOP) {
        bubbleView.pointerPosition = when (location) {
            Location.TOP -> {
                BubblePosition.BOTTOM
            }
            Location.BOTTOM -> {
                BubblePosition.TOP
            }
            Location.START -> {
                BubblePosition.RIGHT
            }
            Location.END -> {
                BubblePosition.LEFT
            }
        }
        if (onMeasured) {
            showBubbleImmediately(targetView, location)
        } else {
            bubbleView.onGlobalLayout {
                onMeasured = true
                showBubbleImmediately(targetView, location)
            }
        }

    }

    private fun showBubbleImmediately(targetView: View?, location: Location) {
        val viewLocation = intArrayOf(0, 0)
        targetView?.getLocationInWindow(viewLocation)
        when (location) {
            Location.TOP, Location.BOTTOM -> {
                bubbleView.x = viewLocation[0].toFloat() -
                        (bubbleView.getBubbleWidth() / 2) +
                        ((targetView?.let {
                            it.measuredWidth - it.paddingStart + it.paddingEnd
                        } ?: 0) / 2)
                bubbleView.y = if (location == Location.TOP) {
                    viewLocation[1].toFloat() - bubbleView.getBubbleHeight()
                } else {
                    viewLocation[1].toFloat() + (targetView?.let {
                        it.measuredHeight - it.paddingTop + it.paddingBottom
                    } ?: 0)
                }
            }
            Location.START, Location.END -> {
                bubbleView.x = if (location == Location.START) {
                    viewLocation[0].toFloat() - bubbleView.getBubbleWidth()
                } else {
                    viewLocation[0].toFloat() + (targetView?.let {
                        it.measuredHeight - it.paddingTop + it.paddingBottom
                    } ?: 0)
                }
                bubbleView.y = viewLocation[1].toFloat() -
                        (bubbleView.getBubbleHeight() / 2) +
                        ((targetView?.let {
                            it.measuredWidth - it.paddingStart + it.paddingEnd
                        } ?: 0) / 2)
            }
        }
        bubbleView.showBubble()
    }

    inner class BubbleView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
    ) : FrameLayout(context, attrs) {

        private val mBubbleRect = RectF()
        fun getBubbleWidth(): Float = mBubbleRect.right
        fun getBubbleHeight(): Float = mBubbleRect.bottom

        private val mPointerPath = Path()

        private val mPaint = Paint().apply {
            this.color = Color.WHITE
            this.isAntiAlias = true
            this.isDither = true
            this.strokeJoin = Paint.Join.MITER
            this.strokeCap = Paint.Cap.SQUARE
            this.style = Paint.Style.FILL
            this.maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.SOLID)
        }

        var radius = 5f
            private set

        var pointerHeight = 0f
        var pointerWidth = 0f

        private val bubblePadding = Rect(-1, -1, -1, -1)

        var pointerPosition = BubblePosition.BOTTOM
            set(value) {
                markShape(measuredWidth.toFloat(), measuredHeight.toFloat())
                textView?.layout(
                    mBubbleRect.left.toInt(),
                    mBubbleRect.top.toInt(),
                    mBubbleRect.right.toInt(),
                    mBubbleRect.bottom.toInt()
                )
                field = value
            }
        var pointerGravity = BubbleGravity.CENTER

        private var textView: TextView? = null

        var text: CharSequence? = null
            set(value) {
                textView?.text = value
                markShape(textView?.width?.toFloat() ?: 0f, textView?.height?.toFloat() ?: 0f)
                invalidate()
                field = null
            }
            get() = textView?.text

        init {
            setBackgroundColor(Color.TRANSPARENT)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            markShape(w.toFloat(), h.toFloat())
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            canvas?.drawPath(mPointerPath, mPaint)
            canvas?.drawRoundRect(mBubbleRect, radius, radius, mPaint)
        }

        private fun markShape(w: Float, h: Float) {
            if (pointerWidth == 0f) {
                pointerWidth = w / 5f
            }
            if (pointerHeight == 0f) {
                pointerHeight = h / 8f
            }

            bubblePadding.apply {
                if (left == -1) {
                    left = (pointerWidth / 10).toInt()
                }
                if (right == -1) {
                    right = left
                }
                if (top == -1) {
                    top = (pointerHeight / 10).toInt()
                }
                if (bottom == -1) {
                    bottom = top
                }
            }

            var l = 0f
            var t = 0f
            var r = w
            var b = h
            mPointerPath.reset()
            when (pointerPosition) {
                BubblePosition.BOTTOM, BubblePosition.TOP -> {
                    val y = if (pointerPosition == BubblePosition.BOTTOM) {
                        b = h - pointerHeight
                        b
                    } else {
                        t += pointerHeight
                        t
                    }
                    val strokeY = if (pointerPosition == BubblePosition.BOTTOM) h else 0f
                    when (pointerGravity) {
                        BubbleGravity.START -> {
                            mPointerPath.moveTo(radius, y)
                            mPointerPath.lineTo(pointerWidth + radius, y)
                            mPointerPath.lineTo(pointerWidth / 2 + radius, strokeY)
                        }
                        BubbleGravity.CENTER -> {
                            val halfW = w / 2f
                            mPointerPath.moveTo(halfW - pointerWidth / 2 + radius, y)
                            mPointerPath.lineTo(halfW + pointerWidth / 2 - radius, y)
                            mPointerPath.lineTo(halfW, strokeY)
                        }
                        BubbleGravity.END -> {
                            mPointerPath.moveTo(w - pointerWidth - radius, y)
                            mPointerPath.lineTo(w - radius, y)
                            mPointerPath.lineTo(w - pointerWidth / 2 + radius, strokeY)
                        }
                    }
                }
                BubblePosition.LEFT, BubblePosition.RIGHT -> {
                    val x = if (pointerPosition == BubblePosition.LEFT) {
                        l += pointerHeight
                        l
                    } else {
                        r = w - pointerHeight
                        r
                    }
                    val strokeX = if (pointerPosition == BubblePosition.LEFT) 0f else w
                    when (pointerGravity) {
                        BubbleGravity.START -> {
                            mPointerPath.moveTo(x, radius)
                            mPointerPath.lineTo(x, pointerWidth + radius)
                            mPointerPath.lineTo(strokeX, pointerWidth / 2 + radius)
                        }
                        BubbleGravity.CENTER -> {
                            val halfH = h / 2f
                            mPointerPath.moveTo(x, halfH - pointerWidth / 2 + radius)
                            mPointerPath.lineTo(x, halfH + pointerWidth / 2 - radius)
                            mPointerPath.lineTo(strokeX, halfH)
                        }
                        BubbleGravity.END -> {
                            mPointerPath.moveTo(x, h - pointerWidth - radius)
                            mPointerPath.lineTo(x, h - radius)
                            mPointerPath.lineTo(strokeX, h - pointerWidth / 2 + radius)
                        }
                    }
                }
            }
            mBubbleRect.set(l, t, r, b)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            if (childCount <= 0) return
            for (i in 0 until childCount) {
                getChildAt(i).also {
                    if (it is TextView) {
                        textView = it
                        it.layoutParams = (it.layoutParams as MarginLayoutParams).let { params ->
                            params.height =
                                params.height - bubblePadding.top - bubblePadding.bottom
                            params.width =
                                params.width - bubblePadding.left - bubblePadding.right
                            params.bottomMargin = pointerHeight.toInt()
                            params
                        }

                        it.layout(
                            mBubbleRect.left.toInt(),
                            mBubbleRect.top.toInt(),
                            mBubbleRect.right.toInt(),
                            mBubbleRect.bottom.toInt()
                        )
                    }
                }
            }
        }

        fun setBubbleRadius(r: Float) {
            radius = r
            markShape(width.toFloat(), height.toFloat())
            invalidate()
        }

        private val animateHandler = Handler(Looper.getMainLooper()) {
            if (it.what == 1) {
                animate().alpha(0f).setDuration(200)
                    .setInterpolator(AccelerateInterpolator())
                    .start()
            }
            true
        }

        fun showBubble() {
            animate().alpha(1f).setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    animateHandler.removeMessages(1)
                    animateHandler.sendEmptyMessageDelayed(1, 500)
                }.start()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            animateHandler.removeCallbacksAndMessages(null)
        }
    }
}



