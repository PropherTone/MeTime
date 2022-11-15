package com.protone.base.view.customView

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class RotateGradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var len = 0
    private var h = 0
    private var w = 0

    init {
        val gradientView = GradientChildView(context, attrs, defStyleAttr)
        gradientView.rotation = rotation
        rotation = 0f
        addView(gradientView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        w = MeasureSpec.getSize(widthMeasureSpec)
        h = MeasureSpec.getSize(heightMeasureSpec)
        len = if (w > h) {
            w
        } else {
            h
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (childCount > 0) {
            val childAt = getChildAt(0)
            if (childAt is GradientChildView) {
                len = (len * 1.5).toInt()
                val halfH = h / 2
                val halfW = w / 2
                val halfLen = len / 2
                val diveH = halfLen - halfH
                val diveW = halfLen - halfW
                childAt.layout(-diveW, -diveH, len, len)
            }
        }
    }
}