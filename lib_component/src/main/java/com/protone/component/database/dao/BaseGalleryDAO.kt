package com.protone.component.database.dao

import android.net.Uri
import android.util.Log
import com.protone.common.baseType.withIOContext
import com.protone.common.entity.*
import com.protone.component.database.MediaAction
import com.protone.database.room.*

sealed class BaseGalleryDAO : SignedGalleryDAO()

sealed class SignedGalleryDAO : MediasWithGalleriesDAO() {
    private val signedGalleryDAO = getGalleryDAO()

    suspend fun getAllSignedMedia(): List<GalleryMedia>? = withIOContext {
        signedGalleryDAO.getAllSignedMedia()
    }

    suspend fun getAllMediaByType(isVideo: Boolean): List<GalleryMedia>? = withIOContext {
        signedGalleryDAO.getAllMediaByType(isVideo)
    }

    suspend fun getAllMediaBetweenDate(start: Long, end: Long): List<GalleryMedia>? =
        withIOContext {
            signedGalleryDAO.getAllMediaBetweenDate(start, end)
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

    suspend fun getMediaCount(isVideo: Boolean): Int =
        withIOContext {
            signedGalleryDAO.getMediaCount(isVideo)
        }

    suspend fun getMediaCount(): Int = withIOContext { signedGalleryDAO.getMediaCount() }

    suspend fun getMediaCountByGallery(name: String): Int =
        withIOContext {
            signedGalleryDAO.getMediaCountByGallery(name)
        }

    suspend fun getNewestMediaInGallery(gallery: String): Uri? =
        withIOContext {
            signedGalleryDAO.getNewestMediaInGallery(gallery)
        }

    suspend fun getNewestMedia(): Uri? = withIOContext { signedGalleryDAO.getNewestMedia() }

    suspend fun getNewestMediaInGallery(gallery: String, isVideo: Boolean): Uri? =
        withIOContext {
            signedGalleryDAO.getNewestMediaInGallery(gallery, isVideo)
        }

    suspend fun getNewestMedia(isVideo: Boolean): Uri? =
        withIOContext { signedGalleryDAO.getNewestMedia(isVideo) }

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

    suspend fun getSignedMedia(id: Long): GalleryMedia? = withIOContext {
        signedGalleryDAO.getSignedMedia(id)
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

sealed class GalleryBucketDAO : BaseDAO<MediaAction.GalleryDataAction>() {
    private val galleryBucketDAO = getGalleryBucketDAO()

    suspend fun getGalleryBucket(name: String): GalleryBucket? = withIOContext {
        galleryBucketDAO.getGalleryBucket(name)
    }

    suspend fun getGalleryBucket(id: Long): GalleryBucket? = withIOContext {
        galleryBucketDAO.getGalleryBucket(id)
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

sealed class MediasWithGalleriesDAO : GalleriesWithNotesDAO() {
    private val mediaWithGalleryBucketDAO by lazy { getMediaWithGalleryBucketDAO() }

    suspend fun insertMediaWithGalleryBucket(
        mediaWithGalleryBucket: MediaWithGalleryBucket,
        media: GalleryMedia
    ): Long = withIOContext {
        mediaWithGalleryBucketDAO.insertMediaWithGalleryBucket(mediaWithGalleryBucket).apply {
            sendEvent(
                MediaAction.GalleryDataAction.OnMediaWithGalleryBucketInserted(
                    mediaWithGalleryBucket.galleryBucketId,
                    media
                )
            )
        }
    }

    suspend fun insertMediaWithGalleryBucketMulti(
        mediaWithGalleryBuckets: List<MediaWithGalleryBucket>,
        medias: List<GalleryMedia>
    ): List<Long> = withIOContext {
        return@withIOContext mediaWithGalleryBucketDAO.insertMediaWithGalleryBucketMulti(
            mediaWithGalleryBuckets
        ).apply {
            sendEvent(
                MediaAction.GalleryDataAction.OnMediaWithGalleryBucketMultiInserted(
                    mediaWithGalleryBuckets,
                    medias
                )
            )
        }
    }

    suspend fun deleteMediaWithGalleryBucket(mediaWithGalleryBucket: MediaWithGalleryBucket): Int =
        withIOContext {
            return@withIOContext mediaWithGalleryBucketDAO.deleteMediaWithGalleryBucket(
                mediaWithGalleryBucket.mediaId,
                mediaWithGalleryBucket.galleryBucketId
            ).apply {
                sendEvent(
                    MediaAction.GalleryDataAction.OnMediaWithGalleryBucketDeleted(
                        mediaWithGalleryBucket
                    )
                )
            }
        }


    suspend fun getGalleryMediasByBucket(bucketId: Long): List<GalleryMedia>? = withIOContext {
        return@withIOContext mediaWithGalleryBucketDAO.getGalleryMediasByBucket(bucketId)
    }

    suspend fun getGalleryBucketByMedias(mediaId: Long): List<GalleryBucket>? = withIOContext {
        return@withIOContext mediaWithGalleryBucketDAO.getGalleryBucketByMedias(mediaId)
    }
}

