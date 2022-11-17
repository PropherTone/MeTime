package com.protone.ui.view.customView.blurView

import android.graphics.Bitmap

abstract class BlurEngine {
    companion object {
        const val DEFAULT_SCALE_FACTOR = 5f
        const val DEFAULT_BITMAP_RENDER_ROUNDED = 16f
    }

    var blurRadius = 24f
        private set

    var scaleFactor = -1f

    fun setRadius(radius: Float) {
        if (radius !in 1f..24f) {
            throw IllegalArgumentException("Blur Radius must in 1..24")
        }
        this.blurRadius = radius
    }

    abstract fun blur(bitmap: Bitmap): Bitmap?
    abstract fun getScaleFactor(w: Int): Float
    abstract fun getBitmapConfig(): Bitmap.Config
    abstract fun release()
}