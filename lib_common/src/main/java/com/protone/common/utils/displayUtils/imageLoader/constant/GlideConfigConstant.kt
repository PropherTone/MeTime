package com.protone.common.utils.displayUtils.imageLoader.constant

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

object GlideConfigConstant {

    private val constants by lazy { mutableMapOf<String, ConfigConstant>() }

    internal fun clear() {
        constants.clear()
    }

    fun centerCrop(isOptional: Boolean): CenterCrop =
        constants["CenterCrop"]?.let {
            it as CenterCrop?
            it.isOptional = isOptional
            it
        } ?: CenterCrop(isOptional)


    fun circleCrop(isOptional: Boolean): CircleCrop =
        constants["CircleCrop"]?.let {
            it as CircleCrop?
            it.isOptional = isOptional
            it
        } ?: CircleCrop(isOptional)

    fun centerInside(isOptional: Boolean): CenterInside =
        constants["CenterInside"]?.let {
            it as CenterInside?
            it.isOptional = isOptional
            it
        } ?: CenterInside(isOptional)

    fun fitCenter(isOptional: Boolean): FitCenter =
        constants["FitCenter"]?.let {
            it as FitCenter?
            it.isOptional = isOptional
            it
        } ?: FitCenter(isOptional)

    fun diskCacheStrategy(cacheStrategy: Int): DiskCacheStrategy =
        constants["DiskCacheStrategy"]?.let {
            it as DiskCacheStrategy?
            it.cacheStrategy = cacheStrategy
            it
        } ?: DiskCacheStrategy(cacheStrategy)

    fun showErrorById(resId: Int): ShowErrorById =
        constants["ShowErrorById"]?.let {
            it as ShowErrorById?
            it.resId = resId
            it
        } ?: ShowErrorById(resId)

    fun showErrorByDrawable(drawable: Drawable): ShowErrorByDrawable =
        constants["ShowErrorByDrawable"]?.let {
            it as ShowErrorByDrawable?
            it.drawable = drawable
            it
        } ?: ShowErrorByDrawable(drawable)

    fun showFallbackById(resId: Int): ShowFallbackById =
        constants["ShowFallbackById"]?.let {
            it as ShowFallbackById?
            it.resId = resId
            it
        } ?: ShowFallbackById(resId)

    fun showFallbackByDrawable(drawable: Drawable): ShowFallbackByDrawable =
        constants["ShowFallbackByDrawable"]?.let {
            it as ShowFallbackByDrawable?
            it.drawable = drawable
            it
        } ?: ShowFallbackByDrawable(drawable)

    fun showPlaceholderById(resId: Int): ShowPlaceholderById =
        constants["ShowPlaceholderById"]?.let {
            it as ShowPlaceholderById?
            it.resId = resId
            it
        } ?: ShowPlaceholderById(resId)

    fun showPlaceholderByDrawable(drawable: Drawable): ShowPlaceholderByDrawable =
        constants["ShowPlaceholderByDrawable"]?.let {
            it as ShowPlaceholderByDrawable?
            it.drawable = drawable
            it
        } ?: ShowPlaceholderByDrawable(drawable)

    fun crossFadeTransition(duration: Int): CrossFadeTransition =
        constants["CrossFadeTransition"]?.let {
            it as CrossFadeTransition
            it.duration = duration
            it
        } ?: CrossFadeTransition(duration)

    fun overwrite(width: Int, height: Int): Overwrite =
        constants["CrossFadeTransition"]?.let {
            it as Overwrite
            it.width = width
            it.height = height
            it
        } ?: Overwrite(width, height)

    data class CenterCrop(var isOptional: Boolean) : ConfigConstant()
    data class CircleCrop(var isOptional: Boolean) : ConfigConstant()
    data class CenterInside(var isOptional: Boolean) : ConfigConstant()
    data class FitCenter(var isOptional: Boolean) : ConfigConstant()
    object SkipMemoryCache : ConfigConstant()
    data class DiskCacheStrategy(var cacheStrategy: Int) : ConfigConstant() {
        companion object {
            const val ALL = 0
            const val DATA = 1
            const val AUTOMATIC = 2
            const val RESOURCE = 3
            const val NONE = 4
        }
    }

    data class ShowErrorById(@DrawableRes var resId: Int) : ConfigConstant()
    data class ShowErrorByDrawable(var drawable: Drawable) : ConfigConstant()
    data class ShowFallbackById(@DrawableRes var resId: Int) : ConfigConstant()
    data class ShowFallbackByDrawable(var drawable: Drawable) : ConfigConstant()
    data class ShowPlaceholderById(@DrawableRes var resId: Int) : ConfigConstant()
    data class ShowPlaceholderByDrawable(var drawable: Drawable) : ConfigConstant()
    data class CrossFadeTransition(var duration: Int) : ConfigConstant()
    data class Overwrite(var width: Int, var height: Int) : ConfigConstant()
}