package com.protone.component.database.dao

import android.net.Uri
import android.util.Log
import com.protone.common.baseType.withIOContext
import com.protone.component.database.MediaAction
import com.protone.common.entity.GalleriesWithNotes
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Note
import com.protone.database.room.getGalleriesWithNotesDAO
import com.protone.database.room.getGalleryBucketDAO
import com.protone.database.room.getGalleryDAO
import com.protone.database.room.mediasFilterBy

sealed class BaseGalleryDAO : SignedGalleryDAO()

sealed class SignedGalleryDAO : GalleriesWithNotesDAO() {
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

    suspend fun getMediaCountByGallery(name: String, isVideo: Boolean): Int =
        withIOContext {
            signedGalleryDAO.getMediaCountByGallery(name, isVideo)
        }

    suspend fun getMediaCountByGallery(name: String): Int =
        withIOContext {
            signedGalleryDAO.getMediaCountByGallery(name)
        }

    suspend fun getNewestMedia(): Uri? =
        withIOContext {
            signedGalleryDAO.getNewestMedia()
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
        getSignedMedia(uri)?.let {
            signedGalleryDAO.deleteSignedMediaByUri(uri).apply {
                sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaDeleted(it))
            }
        }
    }

    suspend fun deleteSignedMediasByGallery(gallery: String) = withIOContext {
        signedGalleryDAO.deleteSignedMediasByGallery(gallery).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryDeleted(gallery))
        }
    }

    suspend fun deleteSignedMedia(media: GalleryMedia) = withIOContext {
        signedGalleryDAO.deleteSignedMedia(media).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaDeleted(media))
        }
    }

    suspend fun deleteSignedMediaMulti(medias: List<GalleryMedia>): List<Long>? {
        if (medias.isEmpty()) return null
        return withIOContext {
            signedGalleryDAO.deleteSignedMediaMulti(medias).apply {
                sendEvent(
                    MediaAction.GalleryDataAction.OnGalleryMediasDeleted(medias mediasFilterBy this)
                )
            }
        }
    }

    suspend fun insertSignedMediaMulti(medias: List<GalleryMedia>): List<Long>? {
        if (medias.isEmpty()) return null
        return withIOContext {
            signedGalleryDAO.insertSignedMediaMulti(medias).apply {
                sendEvent(
                    MediaAction.GalleryDataAction.OnGalleryMediasInserted(medias mediasFilterBy this)
                )
            }
        }
    }

    suspend fun insertSignedMedia(media: GalleryMedia): Long = withIOContext {
        signedGalleryDAO.insertSignedMedia(media).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaInserted(media))
        }
    }

    suspend fun updateSignedMedia(media: GalleryMedia) = withIOContext {
        signedGalleryDAO.updateSignedMedia(media).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryMediaUpdated(media))
        }
    }

    suspend fun updateSignedMediaMulti(medias: List<GalleryMedia>): List<Long>? {
        if (medias.isEmpty()) return null
        return withIOContext {
            signedGalleryDAO.updateSignedMediaMulti(medias).apply {
                sendEvent(
                    MediaAction.GalleryDataAction.OnGalleryMediasUpdated(medias mediasFilterBy this)
                )
            }
        }
    }
}

sealed class GalleriesWithNotesDAO : GalleryBucketDAO() {
    private val galleriesWithNotesDAO = getGalleriesWithNotesDAO()

    suspend fun insertGalleriesWithNotes(galleriesWithNotes: GalleriesWithNotes) =
        withIOContext {
            galleriesWithNotesDAO.insertGalleriesWithNotes(galleriesWithNotes).apply {
                sendEvent(
                    MediaAction.GalleryDataAction.OnGalleriesWithNotesInserted(
                        galleriesWithNotes
                    )
                )
            }
        }

    suspend fun getNotesWithGallery(uri: Uri): List<Note> = withIOContext {
        galleriesWithNotesDAO.getNotesWithGallery(uri) ?: mutableListOf()
    }

    suspend fun getGalleriesWithNote(noteId: Long): List<GalleryMedia> =
        withIOContext {
            galleriesWithNotesDAO.getGalleriesWithNote(noteId) ?: mutableListOf()
        }

}

sealed class GalleryBucketDAO : BaseDAO<MediaAction.GalleryDataAction>() {
    private val galleryBucketDAO = getGalleryBucketDAO()

    suspend fun getGalleryBucket(name: String): GalleryBucket? = withIOContext {
        galleryBucketDAO.getGalleryBucket(name)
    }

    suspend fun getAllGalleryBucket(isVideo: Boolean): List<GalleryBucket>? =
        withIOContext {
            galleryBucketDAO.getAllGalleryBucket(isVideo)
        }

    suspend fun getAllGalleryBucket(): List<GalleryBucket>? =
        withIOContext {
            galleryBucketDAO.getAllGalleryBucket()
        }

    suspend fun insertGalleryBucket(galleryBucket: GalleryBucket) = withIOContext {
        galleryBucketDAO.insertGalleryBucket(galleryBucket).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryBucketInserted(galleryBucket))
        }
    }

    suspend fun deleteGalleryBucket(galleryBucket: GalleryBucket) = withIOContext {
        galleryBucketDAO.deleteGalleryBucket(galleryBucket).apply {
            sendEvent(MediaAction.GalleryDataAction.OnGalleryBucketDeleted(galleryBucket))
        }
    }
}