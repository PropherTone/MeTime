package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.entity.NoteDirWithNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDirWithNoteDAO {

    @Insert
    fun insertNoteDirWithNote(noteDirWithNotes: NoteDirWithNotes)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Note INNER JOIN NoteDirWithNotes ON Note.noteId = NoteDirWithNotes.noteId WHERE NoteDirWithNotes.noteDirId IS:noteDirId")
    fun getNotesWithNoteDir(noteDirId: Long): List<Note>?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Note INNER JOIN NoteDirWithNotes ON Note.noteId = NoteDirWithNotes.noteId WHERE NoteDirWithNotes.noteDirId IS:noteDirId")
    fun getNotesWithNoteDirFlow(noteDirId: Long): Flow<List<Note>?>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM NoteDir INNER JOIN NoteDirWithNotes ON NoteDir.noteDirId = NoteDirWithNotes.noteDirId WHERE NoteDirWithNotes.noteId IS:noteId")
    fun getNoteDirWithNote(noteId: Long): List<NoteDir>?
}