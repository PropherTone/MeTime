package com.protone.common.entity

import android.net.Uri
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.protone.common.utils.converters.ListTypeConverter
import com.protone.common.utils.converters.UriTypeConverter

@Entity(
    primaryKeys = ["mediaId", "noteId"],
    foreignKeys = [
        ForeignKey(
            entity = GalleryMedia::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = CASCADE
        )
    ], indices = [
        Index(
            value = ["noteId", "mediaId"],
            unique = true
        )
    ]
)
@TypeConverters(ListTypeConverter::class, UriTypeConverter::class)
data class GalleriesWithNotes(
    @ColumnInfo(name = "mediaId")
    val mediaId: Long,
    @ColumnInfo(name = "noteId")
    val noteId: Long
)