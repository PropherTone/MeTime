package com.protone.common.entity

import androidx.room.*
import com.protone.common.utils.converters.ListTypeConverter
import com.protone.common.utils.converters.UriTypeConverter

@Entity(
    primaryKeys = ["mediaId", "galleryBucketId"],
    foreignKeys = [
        ForeignKey(
            entity = GalleryMedia::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GalleryBucket::class,
            parentColumns = ["galleryBucketId"],
            childColumns = ["galleryBucketId"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [
        Index(
            value = ["galleryBucketId", "mediaId"],
            unique = true
        )
    ]
)
@TypeConverters(ListTypeConverter::class, UriTypeConverter::class)
data class MediaWithGalleryBucket(
    @ColumnInfo(name = "mediaId")
    val mediaId: Long,
    @ColumnInfo(name = "galleryBucketId")
    val galleryBucketId: Long
)