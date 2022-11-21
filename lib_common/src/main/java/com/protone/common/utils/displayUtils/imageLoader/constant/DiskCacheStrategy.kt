package com.protone.common.utils.displayUtils.imageLoader.constant

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

object DiskCacheStrategy {
    const val ALL = 0
    const val DATA = 1
    const val AUTOMATIC = 2
    const val RESOURCE = 3
    const val NONE = 4
}

enum class Transition {
    CrossFade;

    val duration: Int = 200

}
