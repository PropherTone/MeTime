package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.MusicBucket

@Dao
interface MusicBucketDAO {

    @Query("SELECT * FROM MusicBucket ORDER BY date ASC")
    fun getAllMusicBucket(): List<MusicBucket>?

    @Query("SELECT * FROM MusicBucket WHERE name IS :name")
    fun getMusicBucketByName(name: String): MusicBucket?

    @Insert
    fun addMusicBucket(musicBucket: MusicBucket)

    @Update
    fun updateMusicBucket(bucket: MusicBucket) : Int

    @Delete
    fun deleteMusicBucket(bucket: MusicBucket)

}