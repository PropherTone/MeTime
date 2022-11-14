package com.protone.common.utils.converters

import android.net.Uri
import androidx.room.TypeConverter
import com.protone.common.utils.json.toUri
import com.protone.common.utils.json.toUriJson

class UriTypeConverter{

    @TypeConverter
    fun stringToObject(value: String): Uri {
        return value.toUri()
    }

    @TypeConverter
    fun objectToString(uri: Uri): String {
        return uri.toUriJson()
    }
}