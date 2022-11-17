package com.protone.ui.view.customView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.protone.common.context.MApplication
import com.protone.common.utils.onResult
import kotlinx.coroutines.*
import java.io.InputStream

class PictureListScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ScrollView(context, attrs, defStyleAttr, defStyleRes), CoroutineScope by MainScope() {

    private val TAG = this::class.java.name

    var loadingDuration: Long = 500
    var loadingNum = 10

    private var visibleIndex = 0

    private lateinit var container: LinearLayout
    private val imageList = mutableListOf<LoadHolder>()
    var dataList: MutableList<Uri>? = null
        set(value) {
            launch {
                initData()
                checkState()
            }
            field = value
        }

    init {
        launch {
            initData()
            cancel()
        }
    }

    private suspend fun initData() {
        if (dataList == null) {
            Log.w(TAG, "PictureListView: No data found! skip layout")
            return
        } else {
            try {
                addView(LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    container = this
                })

                var position = 0
                dataList?.forEach { uri ->
                    LoadHolder(ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            onPreLoad(uri)
                        )
                    }, position++).run {
                        imageList.add(this)
                        container.addView(imageView)
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (dataList != null) {
            launch {
                checkState()
            }
        }
    }

    private suspend fun checkState() = withContext(Dispatchers.IO) {
        val rect = Rect()
        var localVisibleRect: Boolean
        while (true) {
            imageList.forEach {
                localVisibleRect = it.imageView.getLocalVisibleRect(rect)
                if (it.imageView.isVisible != localVisibleRect) {
                    withContext(Dispatchers.Main) {
                        it.imageView.visibility = if (localVisibleRect) {
                            Log.d(TAG, "checkState: ${it.position}")
                            Glide.with(context).load(dataList?.get(it.position)!!)
                                .into(it.imageView)
//                            it.imageView.setImageBitmap(decodeBitmap(dataList?.get(it.position)!!))
//                            visibleIndex = it.position
                            View.VISIBLE
                        } else {
//                            if (it.position > visibleIndex + loadingNum || it.position < visibleIndex - loadingNum) {
//                                val bitmap = (it.imageView.drawable as BitmapDrawable).bitmap
//                                if (null != bitmap && !bitmap.isRecycled) {
//                                    bitmap.recycle()
//                                    it.imageView.setImageDrawable(null)
//                                }
                            it.imageView.setImageBitmap(null)
                            it.imageView.setImageDrawable(null)
                            View.INVISIBLE
//                            }
//                            View.VISIBLE
                        }
                    }
                }
            }
            delay(loadingDuration)
        }
    }

    private suspend fun countFirstLoad(): Int = withContext(Dispatchers.IO) {
        var count = 0
        var mixHeight = 0
        val doubledScreenHeight = MApplication.screenHeight * 2
        val contentResolver = MApplication.app.contentResolver
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        suspendCancellableCoroutine {
            dataList?.run {
                for (i in 0 until this.size) {
                    val uri = this[i]
                    val ois = contentResolver.openInputStream(uri)
                    val decodeStream = BitmapFactory.decodeStream(ois, null, options)
                    mixHeight += options.outHeight
                    count++
                    ois?.close()
                    decodeStream?.recycle()
                    if (mixHeight > doubledScreenHeight) {
                        break
                    }
                }
            }
            it.resumeWith(Result.success(count))
        }
    }

    private val contentResolver by lazy { MApplication.app.contentResolver }

    private suspend fun onPreLoad(uri: Uri) = onResult { c ->
        val ois = contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val bitmap = BitmapFactory.decodeStream(ois, null, options)
        ois?.close()
        bitmap?.recycle()
        c.resumeWith(Result.success(options.outHeight))
    }


    private suspend fun decodeBitmap(uri: Uri) = withContext(Dispatchers.IO) {
        var ois: InputStream? = null
        suspendCancellableCoroutine<Bitmap> {
            try {
                ois = contentResolver.openInputStream(uri)
                it.resumeWith(Result.success(BitmapFactory.decodeStream(ois)))
            } finally {
                ois?.close()
            }
        }
    }

    class LoadHolder(val imageView: ImageView, val position: Int)
}