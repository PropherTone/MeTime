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
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.context.MApplication
import com.protone.common.utils.displayUtils.imageLoader.*
import com.protone.common.utils.displayUtils.imageLoader.constant.*
import java.io.File
import java.util.ArrayDeque

internal class GlideLoaderEngine : AbstractLoaderEngine<RequestBuilder<Drawable>>() {

    override fun into(context: Context, target: ImageView) {
        Glide.with(context).into(target)
    }

    override fun into(fragment: Fragment, target: ImageView) {
        Glide.with(fragment).into(target)
    }

    override fun into(fragmentActivity: FragmentActivity, target: ImageView) {
        Glide.with(fragmentActivity).into(target)
    }

    override fun into(activity: Activity, target: ImageView) {
        Glide.with(activity).into(target)
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

    private fun RequestManager.into(target: ImageView) {
        asDrawable().load().config().let {
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
        requestFactory = RequestFactory()
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

    override fun RequestBuilder<Drawable>.config(): RequestBuilder<Drawable> =
        requestFactory.configs?.let { enableConfig(it) } ?: this

    private fun RequestBuilder<Drawable>.enableConfig(
        configs: ArrayDeque<ConfigConstant>
    ): RequestBuilder<Drawable> =
        configs.poll()?.let {
            scaleType(it)?.cacheOption(it)?.stateImage(it)
        }?.enableConfig(configs) ?: this

    private fun RequestBuilder<Drawable>.scaleType(
        obj: ConfigConstant
    ): RequestBuilder<Drawable>? =
        when (obj) {
            is GlideConfigConstant.CenterCrop ->
                if (obj.isOptional) this.optionalCenterCrop() else this.centerCrop()
            is GlideConfigConstant.CircleCrop ->
                if (obj.isOptional) this.optionalCircleCrop() else this.circleCrop()
            is GlideConfigConstant.CenterInside ->
                if (obj.isOptional) this.optionalCenterInside() else this.centerInside()
            is GlideConfigConstant.FitCenter ->
                if (obj.isOptional) this.optionalFitCenter() else this.fitCenter()
            else -> null
        }

    private fun RequestBuilder<Drawable>.cacheOption(
        obj: ConfigConstant
    ): RequestBuilder<Drawable>? =
        when (obj) {
            is GlideConfigConstant.SkipMemoryCache -> this.skipMemoryCache(true)
            is GlideConfigConstant.DiskCacheStrategy ->
                this.diskCacheStrategy(
                    when (obj.cacheStrategy) {
                        0 -> com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
                        1 -> com.bumptech.glide.load.engine.DiskCacheStrategy.DATA
                        2 -> com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC
                        3 -> com.bumptech.glide.load.engine.DiskCacheStrategy.RESOURCE
                        else -> com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
                    }
                )
            else -> null
        }

    private fun RequestBuilder<Drawable>.stateImage(
        obj: ConfigConstant
    ): RequestBuilder<Drawable>? =
        when (obj) {
            is GlideConfigConstant.ShowErrorById ->
                this.error(obj.resId)
            is GlideConfigConstant.ShowErrorByDrawable ->
                this.error(obj.drawable)
            is GlideConfigConstant.ShowFallbackById ->
                this.fallback(obj.resId)
            is GlideConfigConstant.ShowFallbackByDrawable ->
                this.fallback(obj.drawable)
            is GlideConfigConstant.ShowPlaceholderById ->
                this.placeholder(obj.resId)
            is GlideConfigConstant.ShowPlaceholderByDrawable ->
                this.placeholder(obj.drawable)
            is GlideConfigConstant.CrossFadeTransition ->
                this.transition(DrawableTransitionOptions.withCrossFade(obj.duration))
            is GlideConfigConstant.Overwrite ->
                this.override(obj.width, obj.height)
            else -> null
        }

    private inline fun <reified T> Any?.checkType(): T? {
        if (this == null) return null
        return if (this is T) this else null
    }

}