package com.protone.common.utils.displayUtils

import android.graphics.Color

data class BlurFactor(
    val width: Int,
    val height: Int,
    val radius: Int = MAX_RADIUS,
    val sampling: Int = DEFAULT_SAMPLING,
    val color: Int = Color.TRANSPARENT
) {
    companion object {
        const val MAX_RADIUS = 25
        const val DEFAULT_SAMPLING = 1
    }
}