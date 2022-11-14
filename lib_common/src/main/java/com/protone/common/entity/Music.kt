package com.protone.common.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.protone.common.utils.converters.ListTypeConverter
import com.protone.common.utils.converters.UriTypeConverter

@Entity
@TypeConverters(UriTypeConverter::class, ListTypeConverter::class)
data class Music(
    @ColumnInfo(name = "musicId")
    val musicId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "size")
    val size: Long,
    @ColumnInfo(name = "album")
    val album: String?,
    @ColumnInfo(name = "albumID")
    val albumID: Uri?,
    @ColumnInfo(name = "artist")
    val artist: String?,
    @ColumnInfo(name = "mimeType")
    val mimeType: String,
    @ColumnInfo(name = "bucketDisplayName")
    val bucketDisplayName: String?,
    @ColumnInfo(name = "displayName")
    val displayName: String?,
    @ColumnInfo(name = "duration")
    val duration: Long,
    @ColumnInfo(name = "year")
    val year: Long,
    @ColumnInfo(name = "uri")
    val uri: Uri
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "musicBaseId")
    var musicBaseId: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Music

        if (title != other.title) return false
        if (size != other.size) return false
        if (album != other.album) return false
        if (albumID != other.albumID) return false
        if (artist != other.artist) return false
        if (mimeType != other.mimeType) return false
        if (bucketDisplayName != other.bucketDisplayName) return false
        if (displayName != other.displayName) return false
        if (duration != other.duration) return false
        if (year != other.year) return false
        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (albumID?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (bucketDisplayName?.hashCode() ?: 0)
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

}

fun getEmptyMusic() = Music(
    0,
    "NO MUSIC",
    0,
    null,
    null,
    null,
    "",
    null,
    null,
    0L,
    0L,
    Uri.EMPTY
)

