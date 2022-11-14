package com.protone.common.entity

import androidx.room.*
import com.protone.common.utils.converters.ListTypeConverter

@Entity(
    primaryKeys = ["musicBucketId", "musicBaseId"],
    foreignKeys = [
        ForeignKey(
            entity = MusicBucket::class,
            parentColumns = ["musicBucketId"],
            childColumns = ["musicBucketId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Music::class,
            parentColumns = ["musicBaseId"],
            childColumns = ["musicBaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [
        Index(
            value = ["musicBaseId", "musicBucketId"],
            unique = true
        )
    ]
)
@TypeConverters(ListTypeConverter::class)
data class MusicWithMusicBucket(
    @ColumnInfo(name = "musicBucketId")
    val musicBucketId: Long,
    @ColumnInfo(name = "musicBaseId")
    val musicBaseId: Long
)