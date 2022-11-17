package com.protone.ui.view.customView

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue

class AutoSizeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    companion object {
        const val TAG = "AutoSizeTextView_TAG"
    }

    private var mW = 0
    private var isGet = false
    private var scale = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mW = MeasureSpec.getSize(widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isGet) {
            scale = textSize / mW
            isGet = true
        }
        measureText(mW)
    }

    private fun measureText(target: Int) {
        val newSize = scale * target
        setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
    }
}