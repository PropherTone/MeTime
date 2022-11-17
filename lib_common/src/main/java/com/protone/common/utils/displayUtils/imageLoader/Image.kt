package com.protone.common.utils.displayUtils.imageLoader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.protone.common.utils.displayUtils.imageLoader.constant.GlideConfigConstant
import com.protone.common.utils.displayUtils.imageLoader.engines.GlideLoaderEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.ImageEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.LoaderEngine
import com.protone.common.utils.displayUtils.imageLoader.engines.RequestEngine
import java.io.File

object Image : LoaderEngine {

    private var imageLoader: ImageEngine? = null
    private fun get(): ImageEngine = imageLoader ?: GlideLoaderEngine().also { imageLoader = it }

    override fun load(path: String?): RequestEngine = get().load(path)

    override fun load(bitmap: Bitmap?): RequestEngine = get().load(bitmap)

    override fun load(drawable: Drawable?): RequestEngine = get().load(drawable)

    override fun load(uri: Uri?): RequestEngine = get().load(uri)

    override fun load(file: File?): RequestEngine = get().load(file)

    override fun load(@RawRes @DrawableRes resId: Int?): RequestEngine = get().load(resId)

    override fun load(model: Any?): RequestEngine = get().load(model)

    override fun load(byteArray: ByteArray?): RequestEngine = get().load(byteArray)

    fun clear() {
        get().clearCache()
        GlideConfigConstant.clear()
        imageLoader = null
    }
}