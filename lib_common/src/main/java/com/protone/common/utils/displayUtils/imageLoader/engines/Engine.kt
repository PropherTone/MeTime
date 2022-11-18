package com.protone.common.utils.displayUtils.imageLoader.engines

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.common.utils.displayUtils.imageLoader.constant.ConfigConstant
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
    fun onTrimMemoryLevel(level: Int): RequestEngine
    fun addConfig(configConstant: ConfigConstant): RequestEngine
    fun into(context: Context, target: ImageView)
    fun into(fragment: Fragment, target: ImageView)
    fun into(fragmentActivity: FragmentActivity, target: ImageView)
    fun into(activity: Activity, target: ImageView)
    fun clearCache()
    fun onTrimMemory()
}