package com.protone.component

import android.view.View
import androidx.core.view.isGone
import androidx.databinding.ViewDataBinding
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.common.context.*
import com.protone.common.database.dao.DatabaseBridge
import com.protone.common.entity.GalleryMedia
import com.protone.component.databinding.GalleryOptionPopBinding
import com.protone.component.dialog.cateDialog
import com.protone.component.dialog.checkListDialog
import com.protone.component.dialog.titleDialog
import com.protone.component.popWindows.ColorfulPopWindow
import com.protone.component.popWindows.GalleryOptionPop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.streams.toList

abstract class BaseMediaActivity<VB : ViewDataBinding, VM : BaseViewModel, T : BaseViewModel.ViewEvent>(
    handleEvent: Boolean
) : BaseActivity<VB, VM, T>(handleEvent),
    View.OnClickListener {

    var popLayout: GalleryOptionPopBinding? = null

    private var pop: GalleryOptionPop? = null

    abstract fun popDelete()
    abstract fun popMoveTo()
    abstract fun popRename()
    abstract fun popSelectAll()
    abstract fun popSetCate()
    abstract fun popIntoBox()

    fun initPop() {
        if (pop == null) {
            popLayout = GalleryOptionPopBinding.inflate(layoutInflater, root, false).apply {
                pop = GalleryOptionPop(this@BaseMediaActivity, root)
                galleryDelete.setOnClickListener(this@BaseMediaActivity)
                galleryMoveTo.setOnClickListener(this@BaseMediaActivity)
                galleryRename.setOnClickListener(this@BaseMediaActivity)
                gallerySelectAll.setOnClickListener(this@BaseMediaActivity)
                gallerySetCate.setOnClickListener(this@BaseMediaActivity)
                galleryIntoBox.setOnClickListener(this@BaseMediaActivity)
            }
        }
    }

    fun showPop(anchor: View, onSelect: Boolean) {
        popLayout?.apply {
            galleryDelete.isGone = onSelect
            galleryMoveTo.isGone = onSelect
            galleryRename.isGone = onSelect
            gallerySetCate.isGone = onSelect
        }
        pop?.showPop(anchor)
    }

    override fun onClick(v: View?) {
        popLayout?.apply {
            when (v) {
                galleryDelete -> popDelete()
                galleryMoveTo -> popMoveTo()
                galleryRename -> popRename()
                gallerySelectAll -> popSelectAll()
                gallerySetCate -> popSetCate()
                galleryIntoBox -> popIntoBox()
            }
        }
        pop?.dismiss()
    }

    fun tryRename(gm: List<GalleryMedia>) {
        when {
            gm.size == 1 -> rename(gm[0])
            gm.size > 1 -> renameMulti(gm)
        }
    }

    fun tryDelete(
        gm: List<GalleryMedia>,
        callBack: (List<GalleryMedia>) -> Unit
    ) {
        when {
            gm.size == 1 -> delete(gm[0], callBack)
            gm.size > 1 -> deleteMulti(gm, callBack)
        }
    }

    private fun rename(gm: GalleryMedia) {
        val mimeType = gm.name.getFileMimeType()
        titleDialog(
            getString(R.string.rename),
            gm.name.replace(mimeType, "")
        ) { name ->
            val result = renameMedia(name + mimeType, gm.uri)
            if (result) {
                R.string.success.getString().toast()
            } else R.string.not_supported.getString().toast()
        }
    }

    private fun renameMulti(gm: List<GalleryMedia>) {
        val reList = arrayListOf<String>()
        titleDialog(
            getString(R.string.rename),
            ""
        ) { name ->
            launchIO {
                gm.forEach {
                    val result = funcForMultiRename(
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
                    checkListDialog(R.string.this_file_op_failed.getString(), reList)
                }
            }
        }
    }

    private fun delete(
        gm: GalleryMedia,
        callBack: (List<GalleryMedia>) -> Unit
    ) {
        launchDefault {
            val result = deleteMedia(gm.uri)
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
        launchDefault {
            val reList = arrayListOf<GalleryMedia>()
            gm.forEach {
                val result = deleteMedia(it.uri)
                if (!result) reList.add(it)
            }
            callBack.invoke(reList)
            if (reList.size > 0) {
                reList.stream().map { it.name }.toList().let {
                    withContext(Dispatchers.Main) {
                        checkListDialog(
                            R.string.this_file_op_failed.getString(),
                            it as MutableList<String>
                        )
                    }
                }
            }
        }
    }

    fun addCate(gms: MutableList<GalleryMedia>) {
        cateDialog(addCate = {
            titleDialog(R.string.addCate.getString(), "") { re ->
                if (re.isEmpty()) {
                    "请输入内容".toast()
                    return@titleDialog
                }
                addCate(re, gms)
            }
        }, addCon = {
            launchDefault {
                startActivityForResult(
                    GalleryActivity::class.intent.also { intent ->
                        intent.putExtra(
                            GalleryViewModel.CHOOSE_MODE,
                            GalleryViewModel.CHOOSE_MEDIA
                        )
                    }
                ).let { result ->
                    val uri = result?.data?.getStringExtra(GalleryViewModel.URI)
                    if (uri != null) {
                        addCate(uri, gms)
                    } else showFailedToast()
                }
            }
        })
    }

    private fun addCate(cate: String, gms: MutableList<GalleryMedia>) {
        gms.let { list ->
            list.forEach { gm ->
                gm.also { g ->
                    if (g.cate == null) g.cate = mutableListOf()
                    (g.cate as MutableList).add(cate)
                }
            }
            DatabaseBridge.instance.galleryDAOBridge.updateMediaMultiAsync(list)
        }
    }

    fun moveTo(
        anchor: View,
        isVideo: Boolean,
        gms: MutableList<GalleryMedia>,
        callback: (String, MutableList<GalleryMedia>) -> Unit
    ) = launchMain {
        val pop = ColorfulPopWindow(this@BaseMediaActivity)
        pop.startListPopup(
            anchor = anchor,
            dataList = DatabaseBridge.instance.galleryDAOBridge.getAllGalleryBucket(isVideo)
                ?.map { it.type }?.toList() as MutableList<String>? ?: mutableListOf()
        ) { re ->
            if (re != null) {
                gms.let { list ->
                    list.forEach {
                        if (it.type == null) it.type = mutableListOf()
                        if (it.type?.contains(re) == false)
                            (it.type as MutableList<String>).add(re)
                        else "${it.name}已存在${it.type}中".toast()
                    }
                    DatabaseBridge.instance.galleryDAOBridge.updateMediaMultiAsync(list)
                    callback.invoke(re, list)
                }
            } else R.string.none.getString().toast()
            pop.dismiss()
        }
    }

}