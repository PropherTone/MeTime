package com.protone.gallery.viewModel

import android.net.Uri
import com.protone.common.baseType.*
import com.protone.component.database.userConfig
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.component.BaseViewModel
import kotlin.streams.toList

class GalleryViewViewModel : BaseViewModel() {

    sealed class GalleryViewEvent : ViewEvent {
        object SetNote : GalleryViewEvent()
        object Share : GalleryViewEvent()
    }

    var curPosition: Int = 0
    lateinit var galleryMedias: MutableList<GalleryMedia>

    suspend fun initGalleryData(gallery: String, isVideo: Boolean, isCustom: Boolean) =
        withDefaultContext {
            galleryMedias = (galleryDAO.let {
                val combine = userConfig.combineGallery
                when {
                    isCustom -> it.getGalleryBucket(gallery)?.galleryBucketId?.let { bucketId ->
                        if (combine) it.getGalleryMediasByBucket(bucketId)
                        else it.getGalleryMediasByBucket(bucketId, isVideo)
                    }
                    combine -> it.getAllSignedMedia()
                    gallery == ALL_GALLERY -> it.getAllSignedMedia()
                    else -> it.getAllMediaByGallery(gallery, isVideo)
                }
            } ?: mutableListOf()) as MutableList<GalleryMedia>
        }

    suspend fun getSignedMedia() = galleryDAO.getSignedMedia(galleryMedias[curPosition].uri)

    suspend fun getNotesWithGallery(mediaId: Long): MutableList<String> =
        withDefaultContext {
            galleryDAO.getNotesWithGallery(mediaId)
                .stream()
                .map { note ->
                    note.title
                }.toList() as MutableList<String>
        }

    suspend fun prepareSharedMedia() = withIOContext {
        getCurrentMedia().let {
            it.uri.imageSaveToDisk(it.name, "SharedMedia")
        }
    }

    suspend fun deleteSharedMedia(path: String) = withIOContext {
        path.deleteFile()
    }

    suspend fun getMediaByUri(uri: Uri) = galleryDAO.getSignedMedia(uri)

    fun getCurrentMedia() = galleryMedias[curPosition]

}