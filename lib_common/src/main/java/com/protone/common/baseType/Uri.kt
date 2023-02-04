package com.protone.common.baseType

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.protone.common.context.MApplication
import com.protone.common.utils.isInDebug
import com.protone.common.utils.onResult
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

suspend fun Uri.toBitmap(
    w: Int = 0,
    h: Int = 0
): Bitmap? = onResult {
    if (this != Uri.EMPTY) it.resumeWith(Result.success(
        toMediaBitmap(w, h) ?: try {
            toBitmapByteArray()?.let { byteArray ->
                val options = BitmapFactory.Options()
                if (w != 0 && h != 0) {
                    options.inJustDecodeBounds = true
                    val bitmap = BitmapFactory.decodeByteArray(
                        byteArray,
                        0,
                        byteArray.size,
                        options
                    )
                    options.inSampleSize =
                        calculateInSampleSize(options.outWidth, options.outHeight, w, h)
                    bitmap.recycle()
                    options.inJustDecodeBounds = false
                }
                BitmapFactory.decodeByteArray(
                    byteArray,
                    0,
                    byteArray.size,
                    options
                )
            }
        } catch (e: IOException) {
            null
        }))
}

fun Uri.toMediaBitmap(w: Int, h: Int): Bitmap? {
    if (this == Uri.EMPTY) return null
    var ois = try {
        MApplication.app.contentResolver.openInputStream(this)
    } catch (e: FileNotFoundException) {
        return null
    }
    val os = ByteArrayOutputStream()
    return try {
        val options = BitmapFactory.Options()
        if (w != 0 && h != 0) {
            options.inJustDecodeBounds = true
            val bitmap = BitmapFactory.decodeStream(ois, null, options)
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, w, h)
            bitmap?.recycle()
            ois?.close()
            ois = MApplication.app.contentResolver.openInputStream(this)
            options.inJustDecodeBounds = false
        }
        BitmapFactory.decodeStream(ois, null, options)
    } catch (e: IOException) {
        if (isInDebug()) e.printStackTrace()
        null
    } finally {
        ois?.close()
        os.close()
    }
}

fun Uri.toBitmapByteArray(): ByteArray? {
    if (this == Uri.EMPTY) return null
    val mediaMetadataRetriever = MediaMetadataRetriever()
    return try {
        mediaMetadataRetriever.run {
            setDataSource(MApplication.app, this@toBitmapByteArray)
            embeddedPicture
        }
    } catch (e: IllegalArgumentException) {
        if (isInDebug()) e.printStackTrace()
        null
    } catch (e: SecurityException) {
        if (isInDebug()) e.printStackTrace()
        null
    } catch (e: RuntimeException) {
        if (isInDebug()) e.printStackTrace()
        null
    } finally {
        mediaMetadataRetriever.release()
    }
}