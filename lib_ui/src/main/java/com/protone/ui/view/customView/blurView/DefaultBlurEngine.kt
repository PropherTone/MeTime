package com.protone.ui.view.customView.blurView

import android.graphics.Bitmap
import com.protone.common.utils.displayUtils.Blur

class DefaultBlurEngine : BlurEngine() {

    override fun blur(bitmap: Bitmap): Bitmap? = Blur.doFastBlur(bitmap, blurRadius)

    private fun calculateRoundedScaleFactor(w: Int, roundedScaleFactor: Float): Float {
        val round = (w / roundedScaleFactor).toInt() / DEFAULT_BITMAP_RENDER_ROUNDED
        if ((round - round.toInt()) == 0f) {
            return roundedScaleFactor
        }
        return calculateRoundedScaleFactor(
            w,
            w / (round.toInt() * DEFAULT_BITMAP_RENDER_ROUNDED)
        )
    }

    override fun getScaleFactor(w: Int): Float {
        return calculateRoundedScaleFactor(
            w,
            if (scaleFactor >= 1f) scaleFactor else DEFAULT_SCALE_FACTOR
        )
    }

    override fun getBitmapConfig(): Bitmap.Config = Bitmap.Config.ARGB_8888
    override fun release() = Unit
}