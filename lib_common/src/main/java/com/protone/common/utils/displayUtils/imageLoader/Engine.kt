package com.protone.common.utils.displayUtils.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import java.io.File

interface ImageEngine : LoaderEngine, RequestEngine

interface LoaderEngine {
    fun load(path: String?): RequestEngine
    fun load(bitmap: Bitmap?): RequestEngine
    fun load(drawable: Drawable?): RequestEngine
    fun load(uri: Uri?): RequestEngine
    fun load(file: File?): RequestEngine
    fun load(@RawRes @DrawableRes resId: Int?): RequestEngine
    fun load(model: Any?): RequestEngine
    fun load(byteArray: ByteArray?): RequestEngine
}

interface RequestEngine {
    fun setInterceptor(requestInterceptor: RequestInterceptor): RequestEngine
    fun enableDiskCache(enable: Boolean): RequestEngine
    fun enableMemoryCache(enable: Boolean): RequestEngine
    fun onTrimMemoryLevel(level: Int): RequestEngine
    fun into(context: Context, target: ImageView)
}