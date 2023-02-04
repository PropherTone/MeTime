package com.protone.common.baseType

import android.net.Uri
import com.protone.common.context.MApplication
import com.protone.common.utils.onResult
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun String.getFileName(): String {
    return this.split("/").run { this[this.size - 1] }
}

fun String.getFileMimeType(): String {
    return "." + this.split(".").let { it[it.size - 1] }
}

fun String.deleteFile(): Boolean {
    if (this.isEmpty()) return true
    try {
        val file = File(this)
        if (file.exists()) {
            if (file.isFile) {
                return file.delete()
            } else if (file.isDirectory) {
                return file.deleteRecursively()
            }
        }
    } catch (e: IOException) {
        return false
    } catch (e: FileNotFoundException) {
        return false
    }
    return false
}

fun String.useAsParentDirToSaveFile(
    fileName: String,
    dir: String? = null,
    onExists: (File) -> Boolean,
    onNewFile: (File) -> Boolean
): String? {
    fun saveFailed(fileName: String): String? {
        "文件${fileName}保存失败!".toast()
        return null
    }
    return try {
        val tempPath =
            if (dir != null) {
                val dirFile = File("$this/$dir/")
                if (!dirFile.exists() && !dirFile.mkdirs()) {
                    return saveFailed(fileName)
                }
                "$this/$dir/$fileName"
            } else "$this/$fileName"
        val file = File(tempPath)
        when {
            file.exists() -> {
                if (onExists.invoke(file)) {
                    tempPath
                } else {
                    saveFailed(fileName)
                }
            }
            file.createNewFile() -> {
                if (onNewFile.invoke(file)) {
                    tempPath
                } else {
                    file.deleteOnExit()
                    saveFailed(fileName)
                }
            }
            else -> {
                saveFailed(fileName)
            }
        }
    } catch (e: IOException) {
        saveFailed(fileName)
    } catch (e: FileNotFoundException) {
        saveFailed(fileName)
    }
}

fun String.getParentPath(): String {
    return this.substring(0, lastIndexOf("/"))
}

suspend fun Uri.imageSaveToFile(
    fileName: String,
    dir: String? = null,
    w: Int = 0,
    h: Int = 0
) = withIOContext {
    if (this == Uri.EMPTY) return@withIOContext null
    toBitmap(w, h)?.let {
        try {
            it.saveToFile(fileName, dir)
        } finally {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }
}

suspend fun Uri.imageSaveToDisk(
    fileName: String,
    dir: String? = null,
    w: Int = 0,
    h: Int = 0
): String? {
    if (this == Uri.EMPTY) return null
    var exists = false
    var mimeType: String
    return onResult {
        val bytes = toBitmapByteArray()
            ?: MApplication.app.contentResolver.openInputStream(this@imageSaveToDisk)
                ?.use { inputStream -> inputStream.readBytes() }
        it.resumeWith(
            Result.success(if (bytes == null) {
            null
        } else MApplication.app.filesDir.absolutePath.useAsParentDirToSaveFile(
            bytes.getMediaMimeType().let { mime ->
                mimeType = mime
                "$fileName.$mimeType"
            },
            dir,
            onExists = { file ->
                if (file.getSHA() == bytes.getSHA()) {
                    true
                } else {
                    exists = true
                    true
                }
            },
            onNewFile = { file ->
                FileOutputStream(file).use { outputStream -> outputStream.write(bytes) }
                true
            }
        )))
    }.let {
        if (it == null && exists) {
            this@imageSaveToDisk.imageSaveToDisk("${fileName}_new", dir, w, h)
        } else it
    }
}