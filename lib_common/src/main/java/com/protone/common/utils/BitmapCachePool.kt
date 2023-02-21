package com.protone.common.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.collection.LruCache
import com.bumptech.glide.Glide
import com.protone.common.baseType.toBitmap
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BitmapCachePool {

    private val blurMemCache by lazy {
        val maxMemory = Runtime.getRuntime().maxMemory()
        BlurMemCache((maxMemory / 8).toInt())
    }

    suspend fun get(path: String): Bitmap? = withDefaultContext {
        if (path.isEmpty()) return@withDefaultContext null
        return@withDefaultContext blurMemCache.get(path) ?: path.toBitmap()?.also {
            blurMemCache.put(path, it)
        }
    }

    suspend fun get(uri: Uri): Bitmap?  {
        val key = generateKey(uri.toString())
        return blurMemCache.get(key) ?: uri.toBitmap()?.let {
            blurMemCache.put(key, it)
            it
        }
    }

    fun remove(uri: Uri) {
        blurMemCache.remove(generateKey(uri.toString()))
    }

    fun remove(key: String) {
        blurMemCache.remove(key)
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