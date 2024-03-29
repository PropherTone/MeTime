package com.protone.database.room.dao

import android.net.Uri
import androidx.room.*
import com.protone.common.entity.GalleriesWithNotes
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Note
import com.protone.common.utils.converters.UriTypeConverter

@Dao
@TypeConverters(UriTypeConverter::class)
interface GalleriesWithNotesDAO {

    @Insert
    fun insertGalleriesWithNotes(galleriesWithNotes: GalleriesWithNotes)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Note INNER JOIN GalleriesWithNotes ON Note.noteId = GalleriesWithNotes.noteId WHERE GalleriesWithNotes.mediaId IS:mediaId")
    fun getNotesWithGallery(mediaId: Long): List<Note>?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM GalleryMedia INNER JOIN GalleriesWithNotes ON GalleryMedia.media_uri = GalleriesWithNotes.mediaId WHERE GalleriesWithNotes.noteId IS:noteId")
    fun getGalleriesWithNote(noteId: Long): List<GalleryMedia>?
}