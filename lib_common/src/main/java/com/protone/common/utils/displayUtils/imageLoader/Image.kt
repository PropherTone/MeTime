package com.protone.common.utils.displayUtils.imageLoader

object Image {

    fun load(path: String?): ImageEngine =
        GlideLoaderEngine().apply { load(path) }

}