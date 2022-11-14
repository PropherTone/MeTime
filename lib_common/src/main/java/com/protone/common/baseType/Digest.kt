package com.protone.common.baseType

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and

fun ByteArray.getSHA(): String? {
    return try {
        String(MessageDigest.getInstance("SHA").digest(this))
    } catch (e: Exception) {
        null
    }
}

fun Bitmap.getSHA(): String? {
    return try {
        MessageDigest.getInstance("SHA").let {
            val bos = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.PNG, 100, bos)
            String(it.digest(bos.toByteArray()))
        }
    } catch (e: Exception) {
        null
    }
}

fun File.getSHA(): String? {
    var fis: FileInputStream? = null
    return try {
        MessageDigest.getInstance("SHA").let {
            fis = FileInputStream(this)
            val bytes = fis?.readBytes()
            if (bytes != null) {
                String(it.digest(bytes))
            } else null
        }
    } catch (e: Exception) {
        fis?.close()
        null
    } finally {
        try {
            fis?.close()
        } catch (e: IOException) {
        }
    }
}

fun File.getMD5(): String? {
    var fis: FileInputStream? = null
    return try {
        MessageDigest.getInstance("MD5").let {
            fis = FileInputStream(this)
            val bytes = fis?.readBytes()
            if (bytes != null) {
                String(it.digest(bytes))
            } else null
        }
    } catch (e: Exception) {
        fis?.close()
        null
    } finally {
        try {
            fis?.close()
        } catch (e: IOException) {
        }
    }
}

fun ByteArray.toHexString(): String {
    val stringBuilder = StringBuilder()
    if (this.isEmpty()) return ""
    this.forEach {
        val hexString = Integer.toHexString((it and 0xFF.toByte()).toInt())
        if (hexString.length < 2) {
            stringBuilder.append(0)
        }
        stringBuilder.append(hexString)
    }
    return stringBuilder.toString()
}

fun ByteArray.getMediaMimeType(): String {
    val hexString = this.toHexString()
    return mutableMapOf<String, String>().apply {
        put("FFD8FF", "jpg") // JPEG (jpg)
        put("89504E47", "png") // PNG (png)
        put("47494638", "gif") // GIF (gif)
        put("49492A00227105008037", "tif") // TIFF (tif)
        put("424D228C010000000000", "bmp") // 16色位图(bmp)
        put("424D8240090000000000", "bmp") // 24位位图(bmp)
        put("424D8E1B030000000000", "bmp") // 256色位图(bmp)
        put("41433130313500000000", "dwg") // CAD (dwg)
    }.map {
        if (it.value == hexString.uppercase(Locale.ROOT)) it.value else "png"
    }.takeIf { it.isNotEmpty() }?.get(0) ?: "png"
}