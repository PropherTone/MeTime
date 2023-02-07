package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.MusicBucket
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicBucketDAO {

    @Query("SELECT * FROM MusicBucket ORDER BY date ASC")
    fun getAllMusicBucketFlow(): Flow<List<MusicBucket>?>

    @Query("SELECT * FROM MusicBucket WHERE name IS :name")
    fun getMusicBucketFlowByName(name: String): Flow<MusicBucket?>

    @Query("SELECT * FROM MusicBucket ORDER BY date ASC")
    fun getAllMusicBucket(): List<MusicBucket>?

    @Query("SELECT * FROM MusicBucket WHERE name IS :name")
    fun getMusicBucketByName(name: String): MusicBucket?

    @Query("SELECT * FROM MusicBucket WHERE musicBucketId IS :id")
    fun getMusicBucketById(id: Long): MusicBucket?

    @Insert
    fun addMusicBucket(musicBucket: MusicBucket)

    @Update
    fun updateMusicBucket(bucket: MusicBucket): Int

    @Delete
    fun deleteMusicBucket(bucket: MusicBucket)

}