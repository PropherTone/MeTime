package com.protone.component

import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.alibaba.android.arouter.facade.Postcard
import com.protone.common.context.intent
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.galleryMainPostcard
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.galleryViewPostcard
import com.protone.component.activity.BaseActivity

fun BaseActivity<*, *, *>.toGallery(postcard: (Postcard.() -> Postcard)? = null) =
    postcard?.let { startActivity(RouterPath.GalleryRouterPath.Main, it) }
        ?: startActivity(RouterPath.GalleryRouterPath.Main)

fun BaseFragment<*, *>.toGallery(postcard: (Postcard.() -> Postcard)? = null) =
    postcard?.let { startActivity(RouterPath.GalleryRouterPath.Main, it) }
        ?: startActivity(RouterPath.GalleryRouterPath.Main)

suspend fun BaseActivity<*, *, *>.toGallery(chooseMode: String) =
    startActivityForResult(RouterPath.GalleryRouterPath.Main) {
        galleryMainPostcard(chooseMode)
    }

suspend fun BaseFragment<*, *>.toGallery(chooseMode: String) =
    startActivityForResult(RouterPath.GalleryRouterPath.Main) {
        galleryMainPostcard(chooseMode)
    }

fun BaseActivity<*, *, *>.toGalleryView(
    mediaJson: String,
    isVideo: Boolean,
    isCustom: Boolean,
    targetGallery: String = ALL_GALLERY,
    elementView: View? = null
) {
    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
        galleryViewPostcard(mediaJson, isVideo, isCustom, targetGallery).let {
            elementView?.let { view ->
                it.withOptionsCompat(
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@toGalleryView,
                        view,
                        "galleryMediaItemView"
                    )
                )
            } ?: it
        }
    }
}

fun BaseFragment<*, *>.toGalleryView(
    mediaJson: String,
    isVideo: Boolean,
    isCustom: Boolean,
    targetGallery: String = ALL_GALLERY,
    elementView: View? = null
) {
    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
        galleryViewPostcard(mediaJson, isVideo, isCustom, targetGallery).let {
            elementView?.let view@{ view ->
                val fragmentActivity = activity ?: return@view null
                it.withOptionsCompat(
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        fragmentActivity,
                        view,
                        "galleryMediaItemView"
                    )
                )
            } ?: it
        }
    }
}

fun BaseActivity<*, *, *>.toPictureBox(medias: List<GalleryMedia>) {
    IntentDataHolder.put(medias)
    startActivity(RouterPath.GalleryRouterPath.Box)
}