package com.protone.common.utils.converters

import androidx.room.TypeConverter

class ListTypeConverter {

    @TypeConverter
    fun stringToObject(value: String?): List<String> {
        val mutableList = mutableListOf<String>()
        value?.split("|")?.forEach { if (it.isNotEmpty()) mutableList.add(it) }
        return mutableList
    }

    @TypeConverter
    fun objectToString(list: List<String>?): String {
        val sb = StringBuilder()
        list?.forEach { sb.append("$it|") }
        return sb.toString()
    }
}