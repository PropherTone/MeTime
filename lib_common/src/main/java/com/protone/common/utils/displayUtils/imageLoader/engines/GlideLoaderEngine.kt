package com.protone.common.utils.displayUtils.imageLoader.engines

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.context.MApplication
import com.protone.common.utils.displayUtils.imageLoader.ImageType
import com.protone.common.utils.displayUtils.imageLoader.LoadFailedResult
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.common.utils.displayUtils.imageLoader.constant.Transition
import java.io.File

internal class GlideLoaderEngine : AbstractLoaderEngine<RequestBuilder<Drawable>>() {

    private var manager: RequestBuilder<Drawable>? = null

    override fun with(context: Context): RequestEngine {
        manager = Glide.with(context).asDrawable().load()
        return this
    }

    override fun with(fragment: Fragment): RequestEngine {
        manager = Glide.with(fragment).asDrawable().load()
        return this
    }

    override fun with(fragmentActivity: FragmentActivity): RequestEngine {
        manager = Glide.with(fragmentActivity).asDrawable().load()
        return this
    }

    override fun with(activity: Activity): RequestEngine {
        manager = Glide.with(activity).asDrawable().load()
        return this
    }

    override fun onTrimMemory() {
        Glide.get(MApplication.app).clearMemory()
    }

    override fun setInterceptor(requestInterceptor: RequestInterceptor): RequestEngine {
        manager = manager?.addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                requestInterceptor.onLoadFailed(LoadFailedResult(e, model))
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                requestInterceptor.onLoadSuccess(LoadSuccessResult(resource, model))
                return false
            }
        })
        return this
    }

    override fun centerCrop(isOptional: Boolean): RequestEngine {
        manager = manager?.let { if (isOptional) it.centerCrop() else it.optionalCenterCrop() }
        return this
    }

    override fun circleCrop(isOptional: Boolean): RequestEngine {
        manager = manager?.let { if (isOptional) it.centerCrop() else it.optionalCenterCrop() }
        return this
    }

    override fun centerInside(isOptional: Boolean): RequestEngine {
        manager = manager?.let { if (isOptional) it.centerCrop() else it.optionalCenterCrop() }
        return this
    }

    override fun fitCenter(isOptional: Boolean): RequestEngine {
        manager = manager?.let { if (isOptional) it.centerCrop() else it.optionalCenterCrop() }
        return this
    }

    override fun skipMemoryCache(): RequestEngine {
        manager = manager?.skipMemoryCache(true)
        return this
    }

    override fun diskCacheStrategy(cacheStrategy: Int): RequestEngine {
        manager = manager?.diskCacheStrategy(
            when (cacheStrategy) {
                DiskCacheStrategy.ALL -> com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
                DiskCacheStrategy.DATA -> com.bumptech.glide.load.engine.DiskCacheStrategy.DATA
                DiskCacheStrategy.AUTOMATIC -> com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC
                DiskCacheStrategy.RESOURCE -> com.bumptech.glide.load.engine.DiskCacheStrategy.RESOURCE
                else -> com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
            }
        )
        return this
    }

    override fun error(resId: Int): RequestEngine {
        manager = manager?.error(resId)
        return this
    }

    override fun error(drawable: Drawable): RequestEngine {
        manager = manager?.error(drawable)
        return this
    }

    override fun fallback(resId: Int): RequestEngine {
        manager = manager?.fallback(resId)
        return this
    }

    override fun fallbackBy(drawable: Drawable): RequestEngine {
        manager = manager?.fallback(drawable)
        return this
    }

    override fun placeholder(resId: Int): RequestEngine {
        manager = manager?.placeholder(resId)
        return this
    }

    override fun placeholder(drawable: Drawable): RequestEngine {
        manager = manager?.placeholder(drawable)
        return this
    }

    override fun transition(transition: Transition): RequestEngine {
        manager = manager?.transition(
            when (transition) {
                Transition.CrossFade -> DrawableTransitionOptions.withCrossFade(transition.duration)
            }
        )
        return this
    }

    override fun overwrite(width: Int, height: Int): RequestEngine {
        manager = manager?.override(width, height)
        return this
    }

    override fun into(target: ImageView) {
        manager?.into(target)
    }

    override fun clearCache() {
        Glide.get(MApplication.app).clearDiskCache()
    }

    override fun clearMem() {
        Glide.get(MApplication.app).clearMemory()
    }

    override fun onTrimMemoryLevel(level: Int): RequestEngine {
        Glide.get(MApplication.app).onTrimMemory(level)
        return this
    }

    private fun RequestBuilder<Drawable>.load(): RequestBuilder<Drawable> {
        return when (requestFactory.type) {
            ImageType.String -> {
                load(requestFactory.path.checkType<String?>())
            }
            ImageType.Bitmap -> {
                load(requestFactory.path.checkType<Bitmap?>())
            }
            ImageType.Drawable -> {
                load(requestFactory.path.checkType<Drawable?>())
            }
            ImageType.Uri -> {
                load(requestFactory.path.checkType<Uri?>())
            }
            ImageType.File -> {
                load(requestFactory.path.checkType<File?>())
            }
            ImageType.Int -> {
                load(requestFactory.path.checkType<Int?>())
            }
            ImageType.Any -> {
                load(requestFactory.path)
            }
            ImageType.ByteArray -> {
                load(requestFactory.path.checkType<ByteArray?>())
            }
        }
    }

    private inline fun <reified T> Any?.checkType(): T? {
        if (this == null) return null
        return if (this is T) this else null
    }

}