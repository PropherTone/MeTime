package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.entity.MusicWithMusicBucket

@Dao
interface MusicWithMusicBucketDAO {

    @Insert
    fun insertMusicWithMusicBucket(musicWithMusicBucket: MusicWithMusicBucket): Long?

    @RewriteQueriesToDropUnusedColumns
    @Query("DELETE FROM MusicWithMusicBucket WHERE musicBaseId IS :musicID AND musicBucketId IS:musicBucketId")
    fun deleteMusicWithMusicBucket(musicID: Long,musicBucketId: Long) : Int?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    fun getMusicWithMusicBucket(musicBucketId: Long): List<Music>?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT count(*) FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    fun getMusicWithBucketSize(musicBucketId: Long): Int

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM MusicBucket INNER JOIN MusicWithMusicBucket ON MusicBucket.musicBucketId = MusicWithMusicBucket.musicBucketId WHERE MusicWithMusicBucket.musicBaseId IS:musicID")
    fun getMusicBucketWithMusic(musicID: Long): List<MusicBucket>?
}