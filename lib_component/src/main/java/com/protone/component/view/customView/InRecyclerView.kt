package com.protone.component.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class InRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private var onInterceptTouchEvent: OnTouch? = null

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        e?.let {
            onInterceptTouchEvent?.onTouch(it.x, it.y)
        }
        return super.onInterceptTouchEvent(e)
    }

    fun setOnInterceptTouchEvent(event: OnTouch) {
        this.onInterceptTouchEvent = event
    }

    interface OnTouch {
        fun onTouch(x: Float, y: Float)
    }
}