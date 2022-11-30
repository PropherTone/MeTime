package com.protone.gallery.viewModel

import android.net.Uri
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.component.database.userConfig
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.component.BaseViewModel
import java.util.stream.Collectors
import kotlin.streams.toList

class GalleryViewViewModel : BaseViewModel() {

    sealed class GalleryViewEvent : ViewEvent {
        object SetNote : GalleryViewEvent()
        object Share : GalleryViewEvent()
    }

    var curPosition: Int = 0
    lateinit var galleryMedias: MutableList<GalleryMedia>

    suspend fun initGalleryData(gallery: String, isVideo: Boolean) = withDefaultContext {
        var allMedia = (galleryDAO.let {
            if (userConfig.combineGallery) it.getAllSignedMedia() else it.getAllMediaByType(isVideo)
        } ?: mutableListOf()) as MutableList<GalleryMedia>
        if (gallery != ALL_GALLERY)
            allMedia = allMedia.stream().filter {
                (it.bucket == gallery) || (it.type?.contains(gallery) == true)
            }.collect(Collectors.toList())
        galleryMedias = allMedia
    }

    suspend fun getSignedMedia() = galleryDAO.getSignedMedia(galleryMedias[curPosition].uri)

    suspend fun getNotesWithGallery(mediaUri: Uri): MutableList<String> =
        withDefaultContext {
            galleryDAO.getNotesWithGallery(mediaUri)
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