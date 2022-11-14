package com.protone.database.room.dao

import android.net.Uri
import androidx.room.*
import com.protone.common.utils.converters.UriTypeConverter
import com.protone.common.entity.GalleryMedia

@Dao
@TypeConverters(UriTypeConverter::class)
interface SignedGalleryDAO {

    @Query("SELECT * FROM GalleryMedia ORDER BY dateModified DESC")
    fun getAllSignedMedia(): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE isVideo LIKE :isVideo ORDER BY dateModified DESC")
    fun getAllMediaByType(isVideo: Boolean): List<GalleryMedia>?

    @Query("SELECT DISTINCT bucket FROM GalleryMedia WHERE isVideo LIKE :isVideo ORDER BY dateModified DESC")
    fun getAllGallery(isVideo: Boolean): List<String>?

    @Query("SELECT DISTINCT bucket FROM GalleryMedia ORDER BY dateModified DESC")
    fun getAllGallery(): List<String>?

    @Query("SELECT * FROM GalleryMedia WHERE bucket LIKE :name AND isVideo LIKE :isVideo ORDER BY dateModified DESC")
    fun getAllMediaByGallery(name: String, isVideo: Boolean): List<GalleryMedia>?

    @Query("SELECT * FROM GalleryMedia WHERE bucket LIKE :name ORDER BY dateModified DESC")
    fun getAllMediaByGallery(name: String): List<GalleryMedia>?

    @Query("DELETE FROM GalleryMedia WHERE media_uri LIKE :uri")
    fun deleteSignedMediaByUri(uri: Uri)

    @Query("DELETE FROM GalleryMedia WHERE bucket LIKE :gallery")
    fun deleteSignedMediasByGallery(gallery: String)

    @Delete
    fun deleteSignedMedia(media: GalleryMedia)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSignedMedia(media: GalleryMedia): Long

    @Query("SELECT * FROM GalleryMedia WHERE media_uri LIKE :uri")
    fun getSignedMedia(uri: Uri): GalleryMedia?

    @Query("SELECT * FROM GalleryMedia WHERE path LIKE :path")
    fun getSignedMedia(path: String): GalleryMedia?

    @Update
    fun updateSignedMedia(galleryMedia: GalleryMedia)

}