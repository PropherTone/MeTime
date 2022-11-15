package com.protone.common.utils.displayUtils.imageLoader

import android.graphics.drawable.Drawable

data class LoadSuccessResult(
    val resource: Drawable?,
    val model: Any?
)

data class LoadFailedResult(
    val e: Exception?,
    val model: Any?
)
