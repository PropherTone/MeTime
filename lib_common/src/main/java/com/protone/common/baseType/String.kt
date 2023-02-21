package com.protone.common.baseType

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Editable
import android.widget.Toast
import com.protone.common.context.MApplication
import com.protone.common.context.onUiThread
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

fun String.toBitmap(option: BitmapFactory.Options? = null): Bitmap? {
    return runCatching { BitmapFactory.decodeFile(this, option) }.getOrNull()
}

fun String.toast() {
    MApplication.app.onUiThread {
        Toast.makeText(MApplication.app, this, Toast.LENGTH_SHORT).show()
    }
}

fun Editable.toInt(): Int? = this.toString().let { text ->
    if (text.isNotEmpty()) {
        text.toInt()
    } else null
}
