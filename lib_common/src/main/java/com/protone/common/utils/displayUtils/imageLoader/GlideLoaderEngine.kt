package com.protone.common.utils.displayUtils.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.context.MApplication
import java.io.File

class GlideLoaderEngine : AbstractLoaderEngine() {

    override fun into(context: Context, target: ImageView) {
        Glide.with(context).asDrawable().load().config().let {
            if (requestFactory.requestInterceptor != null) {
                it.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestFactory.requestInterceptor
                            ?.onLoadFailed(LoadFailedResult(e, model))
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        requestFactory.requestInterceptor
                            ?.onLoadSuccess(LoadSuccessResult(resource, model))
                        return false
                    }

                })
            } else it
        }.into(target)
    }

    override fun onTrimMemory() {
        Glide.get(MApplication.app).clearMemory()
    }

    override fun clearCache() {
        Glide.get(MApplication.app).clearDiskCache()
    }

    override fun onTrimMemoryLevel(level: Int): RequestEngine {
        Glide.get(MApplication.app).onTrimMemory(level)
        return this
    }

    private fun RequestBuilder<Drawable>.load(): RequestBuilder<Drawable> {
        return when (requestFactory.type) {
            ImageType.String -> {
                load(requestFactory.path as String?)
            }
            ImageType.Bitmap -> {
                load(requestFactory.path as Bitmap?)
            }
            ImageType.Drawable -> {
                load(requestFactory.path as Drawable?)
            }
            ImageType.Uri -> {
                load(requestFactory.path as Uri?)
            }
            ImageType.File -> {
                load(requestFactory.path as File?)
            }
            ImageType.Int -> {
                load(requestFactory.path as Int?)
            }
            ImageType.Any -> {
                load(requestFactory.path)
            }
            ImageType.ByteArray -> {
                load(requestFactory.path as ByteArray?)
            }
        }
    }

    private fun RequestBuilder<Drawable>.config(): RequestBuilder<Drawable> {
        if (requestFactory.configMap != null) {

        }
    }

}