package com.protone.common.utils.displayUtils.imageLoader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.File

abstract class AbstractLoaderEngine : ImageEngine {

    protected val requestFactory: RequestFactory = RequestFactory()

    override fun load(path: String?): RequestEngine {
        requestFactory.path = path
        requestFactory.type = ImageType.String
        return this
    }

    override fun load(bitmap: Bitmap?): RequestEngine {
        requestFactory.path = bitmap
        requestFactory.type = ImageType.Bitmap
        return this
    }

    override fun load(drawable: Drawable?): RequestEngine {
        requestFactory.path = drawable
        requestFactory.type = ImageType.Drawable
        return this
    }

    override fun load(uri: Uri?): RequestEngine {
        requestFactory.path = uri
        requestFactory.type = ImageType.Uri
        return this
    }

    override fun load(file: File?): RequestEngine {
        requestFactory.path = file
        requestFactory.type = ImageType.File
        return this
    }

    override fun load(resId: Int?): RequestEngine {
        requestFactory.path = resId
        requestFactory.type = ImageType.Int
        return this
    }

    override fun load(model: Any?): RequestEngine {
        requestFactory.path = model
        requestFactory.type = ImageType.Any
        return this
    }

    override fun load(byteArray: ByteArray?): RequestEngine {
        requestFactory.path = byteArray
        requestFactory.type = ImageType.ByteArray
        return this
    }

    override fun setSize(width: Int, height: Int): RequestEngine {
        requestFactory.size = ImageSize(width, height)
        return this
    }

    override fun enableDiskCache(enable: Boolean): RequestEngine {
        requestFactory.enableDiskCache = enable
        return this
    }

    override fun enableMemoryCache(enable: Boolean): RequestEngine {
        requestFactory.enableMemoryCache
        return this
    }

    override fun setInterceptor(requestInterceptor: RequestInterceptor<LoadSuccessResult, LoadFailedResult>): RequestEngine {
        requestFactory.requestInterceptor = requestInterceptor
        return this
    }

    abstract fun onTrimMemory()

    abstract fun clearCache()

}