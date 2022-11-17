package com.protone.common.utils.displayUtils.imageLoader.engines

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.protone.common.utils.displayUtils.imageLoader.*
import com.protone.common.utils.displayUtils.imageLoader.RequestFactory
import com.protone.common.utils.displayUtils.imageLoader.constant.ConfigConstant
import java.io.File
import java.util.ArrayDeque

internal abstract class AbstractLoaderEngine<T> : ImageEngine {

    internal var requestFactory: RequestFactory = RequestFactory()

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

    override fun setInterceptor(requestInterceptor: RequestInterceptor): RequestEngine {
        requestFactory.requestInterceptor = requestInterceptor
        return this
    }

    override fun addConfig(configConstant: ConfigConstant): RequestEngine {
        if (requestFactory.configs == null) requestFactory.configs = ArrayDeque()
        requestFactory.configs?.add(configConstant)
        return this
    }

    protected abstract fun T.config(): T

}