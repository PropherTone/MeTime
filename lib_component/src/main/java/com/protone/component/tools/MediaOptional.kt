package com.protone.component.tools

import android.content.Intent
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import com.protone.common.baseType.*
import com.protone.common.context.*
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.RouterPath
import com.protone.component.R
import com.protone.component.activity.BaseActivity
import com.protone.component.databinding.GalleryOptionPopBinding
import com.protone.component.toGallery
import com.protone.component.view.dialog.cateDialog
import com.protone.component.view.dialog.checkListDialog
import com.protone.component.view.dialog.titleDialog
import com.protone.component.view.popWindows.ColorfulPopWindow
import com.protone.component.view.popWindows.GalleryOptionPop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.streams.toList

interface MediaOptional {
    interface MediaOptions {
        fun popDelete()
        fun popMoveTo()
        fun popRename()
        fun popSetCate()
    }

    fun tryRename(gm: List<GalleryMedia>)
    fun tryDelete(gm: List<GalleryMedia>, callBack: (List<GalleryMedia>) -> Unit)
    fun addCate(gms: List<GalleryMedia>, updateMediaMulti: (List<GalleryMedia>) -> Unit)
    fun moveTo(
        anchor: View,
        gms: List<GalleryMedia>,
        getAllGalleryBucket: () -> List<GalleryBucket>?,
        getGalleryBucket: (String) -> GalleryBucket?,
        insertMediaWithGalleryBucketMulti: (Long, List<GalleryMedia>) -> Unit,
        callback: (GalleryBucket, List<GalleryMedia>) -> Unit
    )

    suspend fun shareGalleryMedia(galleryMedia: GalleryMedia)
}

class MediaOptionalImp(private val activity: BaseActivity<*, *, *>) :
    MediaOptional, View.OnClickListener {

    private val popLayout: GalleryOptionPopBinding by lazy {
        GalleryOptionPopBinding.inflate(activity.layoutInflater, activity.root, false).apply {
            pop = GalleryOptionPop(activity, root)
            galleryDelete.setOnClickListener(this@MediaOptionalImp)
            galleryMoveTo.setOnClickListener(this@MediaOptionalImp)
            galleryRename.setOnClickListener(this@MediaOptionalImp)
            gallerySetCate.setOnClickListener(this@MediaOptionalImp)
        }
    }

    private var pop: GalleryOptionPop? = null

    private var mediaOptions: MediaOptional.MediaOptions? = null

    fun showPop(anchor: View) {
        pop?.showPop(anchor)
    }

    fun doOnBackPressed(): Boolean {
        return if (pop?.isShowing == true) {
            pop?.dismiss()
            true
        } else false
    }

    override fun onClick(v: View?) {
        popLayout.apply {
            when (v) {
                galleryDelete -> mediaOptions?.popDelete()
                galleryMoveTo -> mediaOptions?.popMoveTo()
                galleryRename -> mediaOptions?.popRename()
                gallerySetCate -> mediaOptions?.popSetCate()
            }
        }
        pop?.dismiss()
    }

    override fun tryRename(gm: List<GalleryMedia>) {
        when {
            gm.size == 1 -> rename(gm[0])
            gm.size > 1 -> renameMulti(gm)
        }
    }

    override fun tryDelete(
        gm: List<GalleryMedia>,
        callBack: (List<GalleryMedia>) -> Unit
    ) {
        when {
            gm.size == 1 -> delete(gm[0], callBack)
            gm.size > 1 -> deleteMulti(gm, callBack)
        }
    }

    override fun addCate(gms: List<GalleryMedia>, updateMediaMulti: (List<GalleryMedia>) -> Unit) {
        activity.cateDialog(addCate = {
            activity.titleDialog(R.string.addCate.getString(), "") { re ->
                if (re.isEmpty()) {
                    R.string.enter.getString().toast()
                    return@titleDialog
                }
                addCate(re, gms, updateMediaMulti)
            }
        }, addCon = {
            activity.launchDefault {
                activity.toGallery(RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MEDIA)
                    .let { result ->
                        val uri =
                            result?.getStringExtra(RouterPath.GalleryRouterPath.GalleryMainWire.URI)
                        if (uri != null) {
                            addCate(uri, gms, updateMediaMulti)
                        } else activity.showFailedToast()
                    }
            }
        })
    }

    override fun moveTo(
        anchor: View,
        gms: List<GalleryMedia>,
        getAllGalleryBucket: () -> List<GalleryBucket>?,
        getGalleryBucket: (String) -> GalleryBucket?,
        insertMediaWithGalleryBucketMulti: (Long, List<GalleryMedia>) -> Unit,
        callback: (GalleryBucket, List<GalleryMedia>) -> Unit
    ) {
        activity.launch {
            val pop = ColorfulPopWindow(activity)
            pop.startListPopup(
                anchor = anchor,
                dataList = getAllGalleryBucket()?.map { it.type } ?: listOf()
            ) { re ->
                if (re == null) {
                    R.string.none.getString().toast()
                    pop.dismiss()
                    return@startListPopup
                }
                activity.launchDefault {
                    getGalleryBucket(re)?.let {
                        insertMediaWithGalleryBucketMulti(it.galleryBucketId, gms)
                        withMainContext {
                            callback.invoke(it, gms)
                            pop.dismiss()
                        }
                    }
                }
            }
        }
    }

    override suspend fun shareGalleryMedia(galleryMedia: GalleryMedia) {
        galleryMedia.uri.imageSaveToDisk(galleryMedia.name, "SharedMedia")?.let { path ->
            activity.startActivityForResult(Intent(Intent.ACTION_SEND).apply {
                putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        activity,
                        "com.protone.MeTime.fileProvider",
                        File(path)
                    )
                )
                type = "image/*"
            }).let { path.deleteFile() }
        }
    }

    private fun rename(gm: GalleryMedia) {
        val mimeType = gm.name.getFileMimeType()
        activity.titleDialog(
            R.string.rename.getString(),
            gm.name.replace(mimeType, "")
        ) { name ->
            val result = activity.renameMedia(name + mimeType, gm.uri)
            if (result) {
                R.string.success.getString().toast()
            } else R.string.not_supported.getString().toast()
        }
    }

    private fun renameMulti(gm: List<GalleryMedia>) {
        val reList = arrayListOf<String>()
        activity.titleDialog(
            R.string.rename.getString(),
            ""
        ) { name ->
            activity.launchIO {
                gm.forEach {
                    val result = activity.funcForMultiRename(
                        "$name(${gm.indexOf(it)}).${it.name.getFileMimeType()}",
                        it.uri,
                    )
                    if (result != null) {
                        it.name = result
                    } else {
                        reList.add(it.name)
                    }
                }
                withMainContext {
                    activity.checkListDialog(R.string.this_file_op_failed.getString(), reList)
                }
            }
        }
    }

    private fun delete(
        gm: GalleryMedia,
        callBack: (List<GalleryMedia>) -> Unit
    ) {
        activity.launchDefault {
            val result = activity.deleteMedia(gm.uri)
            if (result) {
                callBack.invoke(mutableListOf(gm))
                R.string.success.getString().toast()
            } else R.string.not_supported.getString().toast()
        }
    }

    private fun deleteMulti(
        gm: List<GalleryMedia>,
        callBack: (List<GalleryMedia>) -> Unit
    ) {
        activity.launchDefault {
            val reList = arrayListOf<GalleryMedia>()
            gm.forEach {
                val result = activity.deleteMedia(it.uri)
                if (!result) reList.add(it)
            }
            callBack.invoke(reList)
            if (reList.size > 0) {
                reList.stream().map { it.name }.toList().let {
                    withContext(Dispatchers.Main) {
                        activity.checkListDialog(
                            R.string.this_file_op_failed.getString(),
                            it as MutableList<String>
                        )
                    }
                }
            }
        }
    }

    private fun addCate(
        cate: String,
        gms: List<GalleryMedia>,
        updateMediaMulti: (List<GalleryMedia>) -> Unit
    ) {
        gms.let { list ->
            list.forEach { gm ->
                gm.also { g ->
                    if (g.cate == null) g.cate = mutableListOf()
                    (g.cate as MutableList).add(cate)
                }
            }
            updateMediaMulti(list)
        }
    }

}