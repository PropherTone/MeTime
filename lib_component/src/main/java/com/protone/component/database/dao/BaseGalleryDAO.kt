package com.protone.component.database.dao

import android.net.Uri
import com.protone.common.baseType.withIOContext
import com.protone.component.database.MediaAction
import com.protone.common.entity.GalleriesWithNotes
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Note
import com.protone.database.room.getGalleriesWithNotesDAO
import com.protone.database.room.getGalleryBucketDAO
import com.protone.database.room.getGalleryDAO

sealed class BaseGalleryDAO : BaseDAO<MediaAction.GalleryDataAction>() {

    private val signedGalleryDAO = getGalleryDAO()

    suspend fun getAllSignedMedia(): List<GalleryMedia>? = withIOContext {
        signedGalleryDAO.getAllSignedMedia()
    }

    suspend fun getAllMediaByType(isVideo: Boolean): List<GalleryMedia>? =
        withIOContext {
            signedGalleryDAO.getAllMediaByType(isVideo)
        }

    suspend fun getAllGallery(isVideo: Boolean): List<String>? = withIOContext {
        signedGalleryDAO.getAllGallery(isVideo)
    }

    suspend fun getAllGallery(): List<String>? = withIOContext {
        signedGalleryDAO.getAllGallery()
    }

    suspend fun getAllMediaByGallery(name: String, isVideo: Boolean): List<GalleryMedia>? =
        withIOContext {
            signedGalleryDAO.getAllMediaByGallery(name, isVideo)
        }

    suspend fun getAllMediaByGallery(name: String): List<GalleryMedia>? =
        withIOContext {
            signedGalleryDAO.getAllMediaByGallery(name)
        }

    suspend fun getSignedMedia(uri: Uri): GalleryMedia? = withIOContext {
        signedGalleryDAO.getSignedMedia(uri)
    }

    suspend fun getSignedMedia(path: String): GalleryMedia? = withIOContext {
        signedGalleryDAO.getSignedMedia(path)
    }

    suspend fun deleteSignedMediaByUri(uri: Uri) = withIOContext {
        getSignedMedia(uri)?.let { sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaDeleted(it)) }
        signedGalleryDAO.deleteSignedMediaByUri(uri)
    }

    suspend fun deleteSignedMediasByGallery(gallery: String) = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryDeleted(gallery))
        signedGalleryDAO.deleteSignedMediasByGallery(gallery)
    }

    suspend fun deleteSignedMedia(media: GalleryMedia) = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaDeleted(media))
        signedGalleryDAO.deleteSignedMedia(media)
    }

    suspend fun insertSignedMedia(media: GalleryMedia): Long = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaInserted(media))
        signedGalleryDAO.insertSignedMedia(media)
    }

    suspend fun updateSignedMedia(media: GalleryMedia) = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaUpdated(media))
        signedGalleryDAO.updateSignedMedia(media)
    }

    /*GalleriesWithNotes********************************************************/
    private val galleriesWithNotesDAO = getGalleriesWithNotesDAO()

    suspend fun insertGalleriesWithNotes(galleriesWithNotes: GalleriesWithNotes) =
        withIOContext {
            sendEvent(MediaAction.GalleryDataAction.OnGalleriesWithNotesInserted(galleriesWithNotes))
            galleriesWithNotesDAO.insertGalleriesWithNotes(galleriesWithNotes)
        }

    suspend fun getNotesWithGallery(uri: Uri): List<Note> = withIOContext {
        galleriesWithNotesDAO.getNotesWithGallery(uri) ?: mutableListOf()
    }

    suspend fun getGalleriesWithNote(noteId: Long): List<GalleryMedia> =
        withIOContext {
            galleriesWithNotesDAO.getGalleriesWithNote(noteId) ?: mutableListOf()
        }

    /*GalleryBucket********************************************************/
    private val galleryBucketDAO = getGalleryBucketDAO()

    suspend fun getGalleryBucket(name: String): GalleryBucket? = withIOContext {
        galleryBucketDAO.getGalleryBucket(name)
    }

    suspend fun getAllGalleryBucket(isVideo: Boolean): List<GalleryBucket>? =
        withIOContext {
            galleryBucketDAO.getAllGalleryBucket(isVideo)
        }

    suspend fun insertGalleryBucket(galleryBucket: GalleryBucket) = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryBucketInserted(galleryBucket))
        galleryBucketDAO.insertGalleryBucket(galleryBucket)
    }

    suspend fun deleteGalleryBucket(galleryBucket: GalleryBucket) = withIOContext {
        sendEvent(MediaAction.GalleryDataAction.OnGalleryBucketDeleted(galleryBucket))
        galleryBucketDAO.deleteGalleryBucket(galleryBucket)
    }
}
