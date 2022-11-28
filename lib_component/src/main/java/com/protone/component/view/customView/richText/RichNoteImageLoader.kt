package com.protone.component.view.customView.richText

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.protone.common.baseType.toBitmap
import com.protone.common.context.MApplication
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.R

class RichNoteImageLoader : IRichNoteImageLoader {

    override fun loadImage(context: Context, uri: Uri?, view: ImageView) {
        TODO("Not yet implemented")
    }

    override fun loadImage(context: Context, path: String?, view: ImageView) {
        val bitmapWH = getWHFromPath(path)
        Image.load(path).with(context)
            .error(R.drawable.ic_baseline_error_outline_24_black)
            .let {
                if (bitmapWH != null) it.overwrite(bitmapWH[0], bitmapWH[1]) else it
            }.into(view)
    }

    override fun loadImage(context: Context, bitmap: Bitmap?, view: ImageView) {
        if (bitmap == null) return
        val bitmapWH = getBitmapWH(bitmap)
        Image.load(bitmap).with(context)
            .error(R.drawable.ic_baseline_error_outline_24_black).let {
                if (bitmapWH != null) it.overwrite(bitmapWH[0], bitmapWH[1]) else it
            }.into(view)
    }

    override fun loadError(context: Context, view: ImageView) {
        Image.load(R.drawable.ic_baseline_error_outline_24_black).with(context).into(view)
    }

    private fun getWHFromPath(path: String?): IntArray? {
        return try {
            val option = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val dba = path?.toBitmap(option) ?: return null
            val height = dba.height
            val width = dba.width
            val index = width / height
            val bmH = MApplication.screenWidth / index
            dba.recycle()
            intArrayOf(MApplication.screenWidth, bmH)
        } catch (e: Exception) {
            null
        }
    }

    private fun getBitmapWH(dba: Bitmap): IntArray? {
        return try {
            val height = dba.height
            val width = dba.width
            val index = width / height
            val bmH = MApplication.screenWidth / index
            intArrayOf(MApplication.screenWidth, bmH)
        } catch (e: Exception) {
            null
        }
    }

}