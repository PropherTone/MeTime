package com.protone.common.context

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.utils.isInDebug

val activityOperationBroadcast: LocalBroadcastManager =
    LocalBroadcastManager.getInstance(MApplication.app)

fun Activity.observeChange(uri: Uri, targetName: String): Boolean {
    var name = ""
    contentResolver.query(
        uri,
        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.also {
        val dn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        while (it.moveToNext()) {
            name = it.getString(dn)
        }
        it.close()
    }
    return name == targetName
}

fun Activity.renameMedia(
    name: String,
    uri: Uri,
): Boolean {
    return try {
        if (observeChange(uri, name)) {
            return true
        }
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        contentResolver.update(
            uri,
            ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    name
                )
            },
            null,
            null
        )
        true
    } catch (e: Exception) {
        if (isInDebug()) e.printStackTrace()
        false
    }
}

fun Activity.funcForMultiRename(
    name: String,
    uri: Uri,
): String? {
    return try {
        if (observeChange(uri, name)) {
            return name
        }
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        contentResolver.update(
            uri,
            ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    name
                )
            },
            null,
            null
        )
        name
    } catch (e: Exception) {
        null
    }
}

fun Activity.deleteMedia(uri: Uri): Boolean {
    return try {
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        contentResolver.delete(
            uri,
            null,
            null
        )
        true
    } catch (e: Exception) {
        false
    }

}

fun Activity.showFailedToast() = runOnUiThread {
    R.string.failed_msg.getString().toast()
}

fun Activity.hideSoftInput() {
    ViewCompat.getWindowInsetsController(window.decorView)?.hide(WindowInsetsCompat.Type.ime())
}

@Suppress("unused")
val Activity.hasNavigationBar: Boolean
    get() {
        return false
//        return this.baseContext.navigationBarHeight > 0
    }

@Suppress("unused")
val Activity.isNavigationBar: Boolean
    get() {
        return false
//        val vp = window.decorView as? ViewGroup
//        if (vp != null) {
//            for (i in 0 until vp.childCount) {
//                vp.getChildAt(i).context.packageName
//                if (vp.getChildAt(i).id != -1 && "navigationBarBackground" ==
//                    resources.getResourceEntryName(vp.getChildAt(i).id)
//                ) return true
//            }
//        }
//        return false
    }

var isKeyBroadShow = false
var onLayoutChangeListener: View.OnLayoutChangeListener? = null

inline fun Activity.setSoftInputStatusListener(crossinline onSoftInput: (Int, Boolean) -> Unit = { _, _ -> }) {
    isKeyBroadShow = false
    removeSoftInputStatusListener()
//    val nvH = if (hasNavigationBar) navigationBarHeight else 0
    val nvH = 0
    onLayoutChangeListener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        val rect = Rect()
        v.getWindowVisibleDisplayFrame(rect)
        val height = root?.height ?: 0
        val i = height - rect.bottom - nvH
        if (i > 0 && !isKeyBroadShow) {
            isKeyBroadShow = true
            onSoftInput.invoke(i + nvH, isKeyBroadShow)
        } else if (i <= 0 && isKeyBroadShow) {
            isKeyBroadShow = false
            onSoftInput.invoke(i + nvH, isKeyBroadShow)
        }
    }
    window.decorView.addOnLayoutChangeListener(onLayoutChangeListener)
}

fun Activity.removeSoftInputStatusListener() {
    if (onLayoutChangeListener == null) return
    window.decorView.removeOnLayoutChangeListener(onLayoutChangeListener)
    onLayoutChangeListener = null
}

@Suppress("DEPRECATION")
fun Activity.setTransparentClipStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isStatusBarContrastEnforced = false
    }
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
}