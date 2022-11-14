package com.protone.common.baseType

import android.graphics.Bitmap
import android.graphics.Matrix
import com.protone.common.context.MApplication
import com.protone.common.utils.isInDebug
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun Bitmap.saveToFile(fileName: String, dir: String? = null): String? {
    return MApplication.app.filesDir.absolutePath.useAsParentDirToSaveFile(fileName, dir,
        onExists = { file ->
            if (file.getSHA() == this.getSHA()) {
                file.path
            } else {
                saveToFile("${fileName}_new.png", dir)
            }
            true
        },
        onNewFile = { file ->
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                this@saveToFile.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                true
            } catch (e: IOException) {
                false
            } catch (e: FileNotFoundException) {
                false
            } finally {
                try {
                    fileOutputStream?.close()
                } catch (e: IOException) {
                    if (isInDebug()) e.printStackTrace()
                }
            }
        })
}

fun getMatrix(h: Int, w: Int, output: Int): Matrix {
    val matrix = Matrix()
    var scale = 1f
    val revers: Boolean
    val shorterLen = if (h > w) {
        revers = true
        w
    } else {
        revers = false
        h
    }

    if (output < shorterLen) {
        scale = output.toFloat() / shorterLen
    }

    if (revers) {
        matrix.setScale(scale, scale)
    } else {
        matrix.setScale(scale, scale)
    }
    return matrix
}

fun calculateInSampleSize(outWidth: Int, outHeight: Int, w: Int, h: Int): Int {
    var sampleSize = 1
    if (outHeight > h || outWidth > w) {
        val halfHeight = outHeight / 2
        val halfWidth = outWidth / 2
        while ((halfHeight / sampleSize) >= h && (halfWidth / sampleSize) >= w) {
            sampleSize *= 2
        }
    }
    return sampleSize
}
