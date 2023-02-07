package com.protone.database.room.dao

import android.net.Uri
import androidx.room.*
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.entity.MusicWithMusicBucket
import com.protone.common.utils.converters.UriTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(UriTypeConverter::class)
interface MusicWithMusicBucketDAO {

    @Insert
    fun insertMusicWithMusicBucket(musicWithMusicBucket: MusicWithMusicBucket): Long?

    @RewriteQueriesToDropUnusedColumns
    @Query("DELETE FROM MusicWithMusicBucket WHERE musicBaseId IS :musicID AND musicBucketId IS:musicBucketId")
    fun deleteMusicWithMusicBucket(musicID: Long, musicBucketId: Long): Int?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    fun getMusicWithMusicBucket(musicBucketId: Long): List<Music>?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    fun getMusicWithMusicBucketFlow(musicBucketId: Long): Flow<List<Music>?>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT uri FROM (SELECT uri,MAX(year) FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId)")
    fun getNewestMusicInBucket(musicBucketId: Long): Uri?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT count(*) FROM Music INNER JOIN MusicWithMusicBucket ON Music.musicBaseId = MusicWithMusicBucket.musicBaseId WHERE MusicWithMusicBucket.musicBucketId IS:musicBucketId")
    fun getMusicWithBucketSize(musicBucketId: Long): Int

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM MusicBucket INNER JOIN MusicWithMusicBucket ON MusicBucket.musicBucketId = MusicWithMusicBucket.musicBucketId WHERE MusicWithMusicBucket.musicBaseId IS:musicID")
    fun getMusicBucketWithMusic(musicID: Long): List<MusicBucket>?
}