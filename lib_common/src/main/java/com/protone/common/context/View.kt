package com.protone.common.context

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

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