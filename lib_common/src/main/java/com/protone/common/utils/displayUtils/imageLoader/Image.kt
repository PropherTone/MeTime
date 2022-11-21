package com.protone.common.utils.displayUtils.imageLoader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.protone.common.utils.displayUtils.imageLoader.engines.ContextEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.GlideLoaderEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.ImageEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.LoaderEngine
import java.io.File

object Image : LoaderEngine {

    private var imageLoader: ImageEngine? = null
    private fun get(): ImageEngine = imageLoader ?: GlideLoaderEngine().also { imageLoader = it }

    override fun load(path: String?): ContextEngine = get().load(path)

    override fun load(bitmap: Bitmap?): ContextEngine = get().load(bitmap)

    override fun load(drawable: Drawable?): ContextEngine = get().load(drawable)

    override fun load(uri: Uri?): ContextEngine = get().load(uri)

    override fun load(file: File?): ContextEngine = get().load(file)

    override fun load(@RawRes @DrawableRes resId: Int?): ContextEngine = get().load(resId)

    override fun load(model: Any?): ContextEngine = get().load(model)

    override fun load(byteArray: ByteArray?): ContextEngine = get().load(byteArray)

    fun clearMem() {
        get().clearMem()
        imageLoader = null
    }

    fun clearDiskCache() {
        get().clearCache()
    }
}