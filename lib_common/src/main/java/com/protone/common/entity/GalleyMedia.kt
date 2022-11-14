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
data class GalleryMedia(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "media_uri")
    val uri: Uri,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "path")
    var path: String?,
    @ColumnInfo(name = "bucket")
    var bucket: String,
    @ColumnInfo(name = "size")
    var size: Long,
    @ColumnInfo(name = "type")
    var type: List<String>?,
    @ColumnInfo(name = "cate")
    var cate: List<String>?,
    @ColumnInfo(name = "date")
    var date: Long,
    @ColumnInfo(name = "dateModified")
    var dateModified: Long,
    @ColumnInfo(name = "thumbnailUri")
    val thumbnailUri: Uri?,
    @ColumnInfo(name = "duration")
    val duration: Long,
    @ColumnInfo(name = "isVideo")
    val isVideo: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GalleryMedia

        if (name != other.name) return false
        if (bucket != other.bucket) return false
        if (size != other.size) return false
        if (uri != other.uri) return false
        if (date != other.date) return false
        if (dateModified != other.dateModified) return false
        if (thumbnailUri != other.thumbnailUri) return false
        if (duration != other.duration) return false
        if (isVideo != other.isVideo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bucket.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + (thumbnailUri?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + isVideo.hashCode()
        return result
    }

    override fun toString(): String {
        return "GalleryMedia(name='$name', path=$path, bucket='$bucket', size=$size, type=$type, cate=$cate, date=$date, dateModified=$dateModified, thumbnailUri=$thumbnailUri, duration=$duration, isVideo=$isVideo)"
    }


}