package com.protone.common.utils.displayUtils.imageLoader.engines

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.protone.common.utils.displayUtils.imageLoader.*
import com.protone.common.utils.displayUtils.imageLoader.RequestFactory
import java.io.File
import java.util.ArrayDeque

internal abstract class AbstractLoaderEngine<T> : ImageEngine {

    internal var requestFactory: RequestFactory = RequestFactory()

    override fun load(path: String?): ContextEngine {
        requestFactory.path = path
        requestFactory.type = ImageType.String
        return this
    }

    override fun load(bitmap: Bitmap?): ContextEngine {
        requestFactory.path = bitmap
        requestFactory.type = ImageType.Bitmap
        return this
    }

    override fun load(drawable: Drawable?): ContextEngine {
        requestFactory.path = drawable
        requestFactory.type = ImageType.Drawable
        return this
    }

    override fun load(uri: Uri?): ContextEngine {
        requestFactory.path = uri
        requestFactory.type = ImageType.Uri
        return this
    }

    override fun load(file: File?): ContextEngine {
        requestFactory.path = file
        requestFactory.type = ImageType.File
        return this
    }

    override fun load(resId: Int?): ContextEngine {
        requestFactory.path = resId
        requestFactory.type = ImageType.Int
        return this
    }

    override fun load(model: Any?): ContextEngine {
        requestFactory.path = model
        requestFactory.type = ImageType.Any
        return this
    }

    override fun load(byteArray: ByteArray?): ContextEngine {
        requestFactory.path = byteArray
        requestFactory.type = ImageType.ByteArray
        return this
    }
}