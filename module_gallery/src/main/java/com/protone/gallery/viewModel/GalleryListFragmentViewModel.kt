package com.protone.gallery.viewModel

import com.protone.common.baseType.withIOContext
import com.protone.common.entity.Gallery
import com.protone.common.utils.ALL_GALLERY
import com.protone.component.BaseViewModel

class GalleryListFragmentViewModel : BaseViewModel() {

    var galleryName = ""
    var isCustom = false

    suspend fun getGalleryData(gallery: Gallery, isVideo: Boolean, combine: Boolean) =
        withIOContext {
            galleryDAO.run {
                if (gallery.custom) return@withIOContext getGalleryBucket(gallery.name)
                    ?.galleryBucketId?.let {
                        if (combine) getGalleryMediasByBucket(it)
                        else getGalleryMediasByBucket(it, isVideo)
                    }
                when {
                    combine && gallery.name == ALL_GALLERY -> getAllSignedMedia()
                    combine -> getAllMediaByGallery(gallery.name)
                    gallery.name == ALL_GALLERY -> getAllMediaByType(isVideo)
                    else -> getAllMediaByGallery(gallery.name, isVideo)
                }
            }
        }

}