package com.protone.common.entity

import android.net.Uri
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.protone.common.utils.converters.ListTypeConverter
import com.protone.common.utils.converters.UriTypeConverter

@Entity(
    primaryKeys = ["media_uri", "noteId"],
    foreignKeys = [
        ForeignKey(
            entity = GalleryMedia::class,
            parentColumns = ["media_uri"],
            childColumns = ["media_uri"],
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
            value = ["noteId", "media_uri"],
            unique = true
        )
    ]
)
@TypeConverters(ListTypeConverter::class, UriTypeConverter::class)
data class GalleriesWithNotes(
    @ColumnInfo(name = "media_uri")
    val uri: Uri,
    @ColumnInfo(name = "noteId")
    val noteId: Long
)