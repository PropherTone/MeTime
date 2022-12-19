package com.protone.common.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.collection.LruCache
import com.protone.common.baseType.toBitmap

class BitmapCachePool {

    private val blurMemCache by lazy {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        BlurMemCache((maxMemory / 8).toInt())
    }

    suspend fun get(uri: Uri): Bitmap? {
        val key = generateKey(uri.toString())
        return blurMemCache.get(key) ?: uri.toBitmap()?.let {
            blurMemCache.put(key, it)
            it
        }
    }

    fun clear() {
        blurMemCache.evictAll()
    }

    private fun generateKey(uri: String): String {
        return uri
    }

}


internal class BlurMemCache(maxSize: Int) : LruCache<String, Bitmap>(maxSize) {
    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.byteCount
    }
}