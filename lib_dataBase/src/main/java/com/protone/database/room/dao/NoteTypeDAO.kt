package com.protone.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.protone.common.entity.NoteDir
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteTypeDAO {

    @Insert
    fun insertNoteDir(noteDir: NoteDir)

    @Query("SELECT * FROM NoteDir WHERE name LIKE :name")
    fun getNoteDir(name: String): NoteDir?

    @Delete
    fun deleteNoteDir(noteDir: NoteDir)

    @Query("SELECT * FROM NoteDir")
    fun getALLNoteDir(): List<NoteDir>?

    @Query("SELECT * FROM NoteDir")
    fun getALLNoteDirFlow(): Flow<List<NoteDir>?>

}