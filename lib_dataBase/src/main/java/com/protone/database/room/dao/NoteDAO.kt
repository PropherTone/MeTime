package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {

    @Query("SELECT * FROM Note ORDER BY Note_Time DESC")
    fun getAllNote(): List<Note>?

    @Query("SELECT * FROM Note ORDER BY Note_Time DESC")
    fun getAllNoteFlow(): Flow<List<Note>>

    @Insert
    fun insertNote(note: Note): Long

    @Query("SELECT * FROM Note WHERE Note_Title LIKE :name")
    fun getNoteByName(name: String): Note?

    @Query("SELECT * FROM Note WHERE noteId LIKE :id")
    fun getNoteById(id: Long): Note?

    @Update
    fun updateNote(note: Note): Int

    @Delete
    fun deleteNote(note: Note): Int

}