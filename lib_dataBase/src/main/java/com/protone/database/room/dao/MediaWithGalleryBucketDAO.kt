package com.protone.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia

@Dao
interface MediaWithGalleryBucketDAO {

    @Query(
        "SELECT * FROM GalleryMedia " +
                "INNER JOIN MediaWithGalleryBucket" +
                " ON GalleryMedia.mediaId = MediaWithGalleryBucket.mediaId" +
                " WHERE MediaWithGalleryBucket.galleryBucketId LIKE :bucketId"
    )
    fun getGalleryMediasByBucket(bucketId: Long): List<GalleryMedia>

    @Query(
        "SELECT * FROM GalleryBucket " +
                "INNER JOIN MediaWithGalleryBucket " +
                "ON GalleryBucket.galleryBucketId = MediaWithGalleryBucket.galleryBucketId " +
                "WHERE MediaWithGalleryBucket.mediaId LIKE :mediaId"
    )
    fun getGalleryBucketByMedias(mediaId: Long): List<GalleryBucket>

}