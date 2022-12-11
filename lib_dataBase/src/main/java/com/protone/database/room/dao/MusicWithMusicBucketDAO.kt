package com.protone.database.room.dao

import androidx.room.*
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.entity.MusicWithMusicBucket

@Dao
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
interface MusicWithMusicBucketDAO {

    @Insert
    fun insertMusicWithMusicBucket(musicWithMusicBucket: MusicWithMusicBucket): Long?

    @Query("DELETE FROM MusicWithMusicBucket WHERE musicBaseId IS :musicID AND musicBucketId IS:musicBucketId")
    @RewriteQueriesToDropUnusedColumns
    fun deleteMusicWithMusicBucket(musicID: Long,musicBucketId: Long)

    @Query("SELECT * FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    @RewriteQueriesToDropUnusedColumns
    fun getMusicWithMusicBucket(musicBucketId: Long): List<Music>?

    @Query("SELECT * FROM MusicBucket INNER JOIN MusicWithMusicBucket ON MusicBucket.musicBucketId = MusicWithMusicBucket.musicBucketId WHERE MusicWithMusicBucket.musicBaseId IS:musicID")
    @RewriteQueriesToDropUnusedColumns
    fun getMusicBucketWithMusic(musicID: Long): List<MusicBucket>?
}