package com.protone.common.context

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Rect
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.utils.displayUtils.Blur
import com.protone.common.utils.displayUtils.BlurFactor

fun View.paddingTop(padding: Int) {
    setPadding(
        paddingLeft,
        paddingTop + padding,
        paddingRight,
        paddingBottom
    )
}

fun View.paddingBottom(padding: Int) {
    setPadding(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom + padding
    )
}

fun View.marginTop(margin: Int) {
    if (this !is ViewGroup) return
    val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
    marginLayoutParams.topMargin += margin
    layoutParams = marginLayoutParams
}

fun View.marginBottom(margin: Int) {
    if (this !is ViewGroup) return
    val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
    marginLayoutParams.bottomMargin = margin
    layoutParams = marginLayoutParams
}

fun View.marginStart(margin: Int) {
    if (this !is ViewGroup) return
    val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
    marginLayoutParams.leftMargin = margin
    layoutParams = marginLayoutParams
}

fun View.marginEnd(margin: Int) {
    if (this !is ViewGroup) return
    val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
    marginLayoutParams.rightMargin = margin
    layoutParams = marginLayoutParams
}

fun ImageView.setBlurBitmap(
    bitmap: Bitmap?,
    radius: Int = Blur.defaultBlurRadius,
    sampling: Int = BlurFactor.DEFAULT_SAMPLING
) = setImageBitmap(bitmap?.let { Blur.blur(it, radius, sampling) })

inline fun View.onGlobalLayout(crossinline block: View.() -> Unit) {
    val view = this
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            block.invoke(view)
        }
    })
}

fun View.clipOutLine(radius: Float) {
    this.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null) return
            if (view.measuredWidth == 0 && view.measuredHeight == 0) return
            val rect = Rect(0, 0, view.measuredWidth, view.measuredHeight)
            outline?.setRoundRect(rect, radius)
        }
    }
    this.clipToOutline = true
}

@SuppressLint("ClickableViewAccessibility")
inline fun RecyclerView.doHoverSelect(crossinline onHoverPosition: (Int) -> Unit) {
    parent.requestDisallowInterceptTouchEvent(true)
    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                findChildViewUnder(event.x, event.y)?.let { child ->
                    getChildViewHolder(child)?.let { vh ->
                        onHoverPosition(vh.layoutPosition)
                    }
                }
                return@setOnTouchListener true
            }
            MotionEvent.ACTION_UP -> {
                setOnTouchListener(null)
            }
            MotionEvent.ACTION_CANCEL -> {
                setOnTouchListener(null)
            }
        }
        return@setOnTouchListener false
    }
}