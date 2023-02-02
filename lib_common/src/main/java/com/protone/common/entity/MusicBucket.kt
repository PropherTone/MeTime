package com.protone.common.entity

import androidx.room.*
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.converters.UriTypeConverter

@Entity
@TypeConverters(UriTypeConverter::class)
data class MusicBucket(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "icon")
    var icon: String?,
    @ColumnInfo(name = "size")
    var size: Int,
    @ColumnInfo(name = "detail")
    var detail: String?,
    @ColumnInfo(name = "date")
    var date: String?
) {

    companion object {
        const val ALL = 0x0F
        const val COVER = 0x01
        const val SIZE = 0x02
        const val NAME = 0x04
        const val DETAIL = 0x08
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "musicBucketId")
    var musicBucketId: Long = 0

    @Ignore
    var tempIcon: String? = null

    @Ignore
    constructor() : this("", null, 0, null, null)

    fun getChangeState(bucket: MusicBucket, ignored: Int? = null): Int {
        var payloads = ALL
        if (ignored != null || ignored == NAME || ignored == COVER || ignored == SIZE || ignored == DETAIL) {
            payloads = payloads xor ignored
        }
        if (this.name == bucket.name && ignored != NAME) {
            payloads = payloads xor NAME
        }
        if (this.icon == bucket.icon && ignored != COVER) {
            payloads = payloads xor COVER
        }
        if (this.size == bucket.size && ignored != SIZE) {
            payloads = payloads xor SIZE
        }
        if (this.detail == bucket.detail && ignored != DETAIL) {
            payloads = payloads xor DETAIL
        }
        return payloads
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MusicBucket

        if (name != other.name) return false
        if (size != other.size) return false
        if (detail != other.detail) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size
        result = 31 * result + (detail?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MusicBucket(name='$name', icon=$icon, size=$size, detail=$detail, date=$date, id=$musicBucketId)"
    }

}