package com.protone.common.baseType

import com.protone.common.utils.sizeFormatMap
import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(format: String = "HH:mm:ss yyyy/MM/dd E"): String? =
    SimpleDateFormat(format, Locale.getDefault()).format(
        Calendar.getInstance(Locale.getDefault()).also {
            it.timeInMillis = this * 1000
        }.time
    )

fun Long.toDateString(): String? =
    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(
        Calendar.getInstance(Locale.getDefault()).also {
            it.timeInMillis = this
        }.time
    )

fun Long.toStringMinuteTime(): String {
    val musicTime: Long = this / 1000
    val sec = musicTime % 60
    return "${musicTime / 60}:${if (sec >= 10) sec else "0$sec"}"
}

fun Long.getFormatStorageSize(): String {
    var i = this@getFormatStorageSize
    var times = 0
    val size = sizeFormatMap.size - 1
    while (i > 1024) {
        i /= 1024
        if (++times >= size) break
    }
    return if (times <= size) "$i${sizeFormatMap[times]}" else {
        "$i${sizeFormatMap[0]}"
    }
}

fun Long.getStorageSize(): String {
    return getSST(0)
}

private fun Long.getSST(times: Int): String {
    if (times >= sizeFormatMap.size - 1) return "$this${sizeFormatMap[0]}"
    if (this < 1024) return "$this${sizeFormatMap[times]}"
    val i = this / 1024
    var count = times
    return i.getSST(++count)
}