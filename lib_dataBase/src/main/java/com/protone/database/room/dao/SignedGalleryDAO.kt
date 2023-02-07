package com.protone.database.room.dao

import android.net.Uri
import android.util.Log
import androidx.room.*
import com.protone.common.entity.Gallery
import com.protone.common.utils.converters.UriTypeConverter
import com.protone.common.entity.GalleryMedia
import com.protone.database.room.mapToLongList
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

@Dao
@TypeConverters(UriTypeConverter::class)
interface SignedGalleryDAO {

    @Query("SELECT * FROM GalleryMedia ORDER BY dateModified DESC")
    fun getAllSignedMedia(): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE isVideo IS :isVideo ORDER BY dateModified DESC")
    fun getAllMediaByTypeObserve(isVideo: Boolean): Flow<List<GalleryMedia>?>

    @Query("SELECT * FROM GalleryMedia WHERE isVideo IS :isVideo ORDER BY dateModified DESC")
    fun getAllMediaByType(isVideo: Boolean): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE dateModified BETWEEN :end AND :start")
    fun getAllMediaBetweenDate(start: Long, end: Long): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE dateModified IS :data")
    fun getAllMediaByDate(data: Long): List<GalleryMedia>?

    @Query("SELECT DISTINCT bucket FROM GalleryMedia WHERE isVideo IS :isVideo ORDER BY dateModified DESC")
    fun getAllGallery(isVideo: Boolean): List<String>?

    @Query("SELECT DISTINCT bucket FROM GalleryMedia ORDER BY dateModified DESC")
    fun getAllGallery(): List<String>?

    @Query("SELECT * FROM GalleryMedia WHERE bucket IS :name AND isVideo IS :isVideo ORDER BY dateModified DESC")
    fun getAllMediaByGallery(name: String, isVideo: Boolean): List<GalleryMedia>?

    @Query("SELECT COUNT(mediaId) FROM GalleryMedia WHERE bucket IS :name AND isVideo IS :isVideo")
    fun getMediaCountByGallery(name: String, isVideo: Boolean): Int

    @Query("SELECT COUNT(mediaId) FROM GalleryMedia WHERE isVideo IS :isVideo")
    fun getMediaCount(isVideo: Boolean): Int

    @Query("SELECT COUNT(mediaId) FROM GalleryMedia")
    fun getMediaCount(): Int

    @Query("SELECT COUNT(mediaId) FROM GalleryMedia WHERE bucket IS :name")
    fun getMediaCountByGallery(name: String): Int

    @Query("SELECT media_uri FROM (SELECT media_uri,MAX(dateModified) FROM GalleryMedia WHERE bucket IS :gallery)")
    fun getNewestMediaInGallery(gallery: String): Uri?

    @Query("SELECT media_uri FROM (SELECT media_uri,MAX(dateModified) FROM GalleryMedia)")
    fun getNewestMedia(): Uri?

    @Query("SELECT media_uri FROM (SELECT media_uri,MAX(dateModified) FROM GalleryMedia WHERE bucket IS :gallery AND isVideo IS :isVideo)")
    fun getNewestMediaInGallery(gallery: String, isVideo: Boolean): Uri?

    @Query("SELECT media_uri FROM (SELECT media_uri,MAX(dateModified) FROM GalleryMedia WHERE isVideo IS :isVideo)")
    fun getNewestMedia(isVideo: Boolean): Uri?

    @Query("SELECT * FROM GalleryMedia WHERE bucket IS :name ORDER BY dateModified DESC")
    fun getAllMediaByGallery(name: String): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE media_uri IS :uri")
    fun getSignedMedia(uri: Uri): GalleryMedia?

    @Query("SELECT * FROM GalleryMedia WHERE path IS :path")
    fun getSignedMedia(path: String): GalleryMedia?

    @Query("SELECT * FROM GalleryMedia WHERE mediaId IS :id")
    fun getSignedMedia(id: Long): GalleryMedia?

    @Query("DELETE FROM GalleryMedia WHERE media_uri IS :uri")
    fun deleteSignedMediaByUri(uri: Uri)

    @Query("DELETE FROM GalleryMedia WHERE bucket IS :gallery")
    fun deleteSignedMediasByGallery(gallery: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSignedMedia(media: GalleryMedia): Long

    @Transaction
    fun insertSignedMediaMulti(medias: List<GalleryMedia>): List<Long> {
        return medias.mapToLongList { insertSignedMedia(it) }
    }

    @Delete
    fun deleteSignedMedia(media: GalleryMedia): Int

    @Transaction
    fun deleteSignedMediaMulti(medias: List<GalleryMedia>): List<Long> {
        return medias.mapToLongList { deleteSignedMedia(it).toLong() }
    }

    @Update
    fun updateSignedMedia(galleryMedia: GalleryMedia): Int

    @Transaction
    fun updateSignedMediaMulti(galleryMedias: List<GalleryMedia>): List<Long> {
        return galleryMedias.mapToLongList { updateSignedMedia(it).toLong() }
    }

}