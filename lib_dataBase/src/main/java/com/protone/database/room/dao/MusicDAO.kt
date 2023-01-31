package com.protone.database.room.dao

import android.net.Uri
import androidx.room.*
import com.protone.common.utils.converters.UriTypeConverter
import com.protone.common.entity.Music
import com.protone.database.room.mapToLongList

@Dao
@TypeConverters(UriTypeConverter::class)
interface MusicDAO {

    @Insert
    fun insertMusic(music: Music): Long

    @Transaction
    fun insertMusicMulti(musics: List<Music>): List<Long> {
        return musics.mapToLongList { insertMusic(it) }
    }

    @Query("SELECT * FROM Music ORDER BY year DESC")
    fun getAllMusic(): List<Music>?

    @Query("SELECT count(musicBaseId) FROM Music")
    fun getAllMusicSize(): Int

    @Query("SELECT uri FROM (SELECT uri,MAX(year) FROM Music)")
    fun getNewestMusicUri(): Uri?

    @Delete
    fun deleteMusic(music: Music): Int

    @Transaction
    fun deleteMusicMulti(musics: List<Music>): List<Long> {
        return musics.mapToLongList { deleteMusic(it).toLong() }
    }

    @Update(entity = Music::class)
    fun updateMusic(music: Music): Int

    @Query("SELECT * FROM Music WHERE uri IS :uri")
    fun getMusicByUri(uri: Uri): Music?

    @Query("SELECT * FROM Music WHERE musicBaseId IS :id")
    fun getMusicById(id: Long): Music?

}