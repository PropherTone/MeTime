package com.protone.common.context

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.protone.common.context.isKeyBroadShow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.newLayoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.root: ViewGroup?
    get() {
        return when (this) {
            is Activity -> {
                findViewById(android.R.id.content)
            }
            else -> null
        }
    }

val Context.statuesBarHeight: Int
    get() {
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        var height = 0
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

val Context.navigationBarHeight: Int
    get() {
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var height = 0
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

@SuppressLint("ClickableViewAccessibility")
fun Context.linkInput(target: View, input: View) {
    target.setOnTouchListener { _, _ ->
        val inputManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isActive) {
            inputManager.hideSoftInputFromWindow(
                input.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            isKeyBroadShow = false
        }
        false
    }
}

inline fun Context.onUiThread(crossinline function: () -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        function.invoke()
        return
    }
    when (this) {
        is Activity -> runOnUiThread { function.invoke() }
        else -> CoroutineScope(Dispatchers.Main).launch { function.invoke() }
    }
}

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
