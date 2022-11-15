package com.protone.base.view.behavior

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import com.protone.common.context.isKeyBroadShow
import com.protone.common.context.navigationBarHeight
import java.lang.ref.WeakReference

class SoftInputStatues(context: Context) {

    private val weakContext: WeakReference<Context> = WeakReference(context)

    private var listener: View.OnLayoutChangeListener? = null

    fun setSoftInputStatuesListener(onSoftInput: (Int, Boolean) -> Unit = { _, _ -> }) {
        weakContext.get()?.let {
            if (it !is Activity) return@let
            isKeyBroadShow = false
            if (listener == null) {
                @Suppress("ObjectLiteralToLambda")
                listener = object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View?,
                        left: Int,
                        top: Int,
                        right: Int,
                        bottom: Int,
                        oldLeft: Int,
                        oldTop: Int,
                        oldRight: Int,
                        oldBottom: Int
                    ) {
                        val rect = Rect()
                        it.window.decorView.getWindowVisibleDisplayFrame(rect)
                        val i = it.window.decorView.height - rect.bottom - it.navigationBarHeight
                        if (i > 0 && !isKeyBroadShow) {
                            isKeyBroadShow = true
                            onSoftInput.invoke(i, i > 0)
                        } else if (i <= 0 && isKeyBroadShow) {
                            isKeyBroadShow = false
                            onSoftInput.invoke(i, false)
                        }
                    }
                }
            }
            it.window.decorView.addOnLayoutChangeListener(listener)
        }
    }

    fun cancel(){
        if (listener != null) {
            weakContext.get()?.let {
                if (it !is Activity) return@let
                it.window.decorView.removeOnLayoutChangeListener(listener)
                listener = null
            }
        }
        weakContext.clear()
    }
}