package com.protone.common.entity

import androidx.room.*
import com.protone.common.utils.converters.ListTypeConverter

@Entity(
    primaryKeys = ["noteDirId", "noteId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteDir::class,
            parentColumns = ["noteDirId"],
            childColumns = ["noteDirId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [
        Index(
            value = ["noteId", "noteDirId"],
            unique = true
        )
    ]
)
@TypeConverters(ListTypeConverter::class)
data class NoteDirWithNotes(
    @ColumnInfo(name = "noteDirId")
    val noteDirId: Long,
    @ColumnInfo(name = "noteId")
    val noteId: Long
)