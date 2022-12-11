package com.protone.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.protone.common.entity.GalleryBucket

@Dao
interface GalleryBucketDAO {

    @Insert
    fun insertGalleryBucket(galleryBucket: GalleryBucket)

    @Query("SELECT * FROM GalleryBucket WHERE type IS :name")
    fun getGalleryBucket(name: String): GalleryBucket?

    @Delete
    fun deleteGalleryBucket(galleryBucket: GalleryBucket)

    @Query("SELECT * FROM GalleryBucket WHERE image IS :isVideo")
    fun getAllGalleryBucket(isVideo: Boolean): List<GalleryBucket>?

    @Query("SELECT * FROM GalleryBucket")
    fun getAllGalleryBucket(): List<GalleryBucket>?

}