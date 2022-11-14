package com.protone.common.baseType

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Editable
import android.widget.Toast
import com.protone.common.context.MApplication
import com.protone.common.context.onUiThread
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

fun String.toBitmap(option: BitmapFactory.Options? = null): Bitmap {
    return BitmapFactory.decodeFile(this, option)
}

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

fun String.toast() {
    MApplication.app.onUiThread {
        Toast.makeText(MApplication.app, this, Toast.LENGTH_SHORT).show()
    }
}

fun Editable.toInt(): Int? = this.toString().let { text ->
    if (text.isNotEmpty()) {
        text.toInt()
    } else null
}
