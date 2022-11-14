package com.protone.common.baseType

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.protone.common.context.MApplication

fun Int.getString(): String {
    return MApplication.app.getString(this)
}

fun Int.getDrawable(): Drawable? {
    return ResourcesCompat.getDrawable(MApplication.app.resources, this, null)
}

fun Int.getColor(): Int {
    return ResourcesCompat.getColor(MApplication.app.resources, this, null)
}

fun Int.toHexColor(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}