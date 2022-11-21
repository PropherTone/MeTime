package com.protone.common.utils.displayUtils.imageLoader.engines

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.common.utils.displayUtils.imageLoader.constant.Transition
import java.io.File

interface ImageEngine : LoaderEngine, ContextEngine, RequestEngine

interface LoaderEngine {
    fun load(path: String?): ContextEngine
    fun load(bitmap: Bitmap?): ContextEngine
    fun load(drawable: Drawable?): ContextEngine
    fun load(uri: Uri?): ContextEngine
    fun load(file: File?): ContextEngine
    fun load(@RawRes @DrawableRes resId: Int?): ContextEngine
    fun load(model: Any?): ContextEngine
    fun load(byteArray: ByteArray?): ContextEngine
}

interface ContextEngine {
    fun with(context: Context): RequestEngine
    fun with(fragment: Fragment): RequestEngine
    fun with(fragmentActivity: FragmentActivity): RequestEngine
    fun with(activity: Activity): RequestEngine
}

interface RequestEngine {
    fun setInterceptor(requestInterceptor: RequestInterceptor): RequestEngine
    fun onTrimMemoryLevel(level: Int): RequestEngine
    fun clearCache()
    fun clearMem()
    fun onTrimMemory()

    fun centerCrop(isOptional: Boolean): RequestEngine
    fun circleCrop(isOptional: Boolean): RequestEngine
    fun centerInside(isOptional: Boolean): RequestEngine
    fun fitCenter(isOptional: Boolean): RequestEngine
    fun skipMemoryCache() : RequestEngine
    fun diskCacheStrategy(cacheStrategy: Int): RequestEngine
    fun error(@DrawableRes resId: Int): RequestEngine
    fun error(drawable: Drawable): RequestEngine
    fun fallback(@DrawableRes resId: Int): RequestEngine
    fun fallbackBy(drawable: Drawable): RequestEngine
    fun placeholder(@DrawableRes resId: Int): RequestEngine
    fun placeholder(drawable: Drawable): RequestEngine
    fun transition(transition: Transition): RequestEngine
    fun overwrite(width: Int, height: Int): RequestEngine

    fun into(@NonNull target: ImageView)
}