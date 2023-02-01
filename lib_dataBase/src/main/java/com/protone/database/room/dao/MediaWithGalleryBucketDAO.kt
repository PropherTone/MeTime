package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.MediaWithGalleryBucket
import com.protone.common.utils.converters.UriTypeConverter
import com.protone.database.room.mapToLongList

@Dao
@TypeConverters(UriTypeConverter::class)
interface MediaWithGalleryBucketDAO {

    @Insert
    fun insertMediaWithGalleryBucket(mediaWithGalleryBucket: MediaWithGalleryBucket): Long

    @Transaction
    fun insertMediaWithGalleryBucketMulti(mediaWithGalleryBuckets: List<MediaWithGalleryBucket>): List<Long> {
        return mediaWithGalleryBuckets.mapToLongList { insertMediaWithGalleryBucket(it) }
    }

    @Query("DELETE FROM MediaWithGalleryBucket WHERE mediaId IS :mediaId AND galleryBucketId IS :galleryBucketId")
    fun deleteMediaWithGalleryBucket(mediaId: Long, galleryBucketId: Long): Int

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM GalleryMedia " +
                "INNER JOIN MediaWithGalleryBucket " +
                "ON GalleryMedia.mediaId = MediaWithGalleryBucket.mediaId " +
                "WHERE MediaWithGalleryBucket.galleryBucketId IS :bucketId"
    )
    fun getGalleryMediasByBucket(bucketId: Long): List<GalleryMedia>?

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM GalleryMedia " +
                "INNER JOIN MediaWithGalleryBucket " +
                "ON GalleryMedia.mediaId = MediaWithGalleryBucket.mediaId " +
                "WHERE MediaWithGalleryBucket.galleryBucketId IS :bucketId " +
                "AND isVideo is :isVideo"
    )
    fun getGalleryMediasByBucket(bucketId: Long, isVideo: Boolean): List<GalleryMedia>?

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM GalleryBucket " +
                "INNER JOIN MediaWithGalleryBucket " +
                "ON GalleryBucket.galleryBucketId = MediaWithGalleryBucket.galleryBucketId " +
                "WHERE MediaWithGalleryBucket.mediaId LIKE :mediaId"
    )
    fun getGalleryBucketByMedias(mediaId: Long): List<GalleryBucket>?

}