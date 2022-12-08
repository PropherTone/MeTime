package com.protone.component.view.dialog

import android.app.Activity
import android.net.Uri
import android.text.Editable
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.protone.common.context.newLayoutInflater
import com.protone.common.context.root
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.onResult
import com.protone.component.R
import com.protone.component.databinding.*
import com.protone.component.view.adapter.BaseAdapter
import com.protone.component.view.adapter.CheckListAdapter
import kotlinx.coroutines.Dispatchers

fun Activity.loginDialog(
    isReg: Boolean,
    loginCall: (String, String) -> Boolean,
    regClk: () -> Boolean
) {
    val binding = LoginPopLayoutBinding.inflate(layoutInflater, root, false)
    if (isReg) binding.btnReg.isGone = true
    val create = AlertDialog.Builder(this).setView(binding.root).create()
    binding.btnLogin.setOnClickListener {
        loginCall.invoke(
            binding.userName.text.toString(),
            binding.userPassword.text.toString()
        ).let { re ->
            if (!re) {
                binding.userNameLayout.isErrorEnabled = true
                binding.userPasswordLayout.isErrorEnabled = true
                binding.userNameLayout.error = " "
                binding.userPasswordLayout.error = " "
            } else {
                create.dismiss()
            }
        }
    }
    binding.btnReg.setOnClickListener {
        create.dismiss()
        regClk.invoke()
    }
    create.show()
}

fun Activity.regDialog(confirmCall: (String, String) -> Boolean) {
    val binding = RegPopLayoutBinding.inflate(layoutInflater, root, false)
    val create = AlertDialog.Builder(this).setView(binding.root).create()
    binding.btnLogin.setOnClickListener {
        confirmCall.invoke(
            binding.userName.text.toString(),
            binding.userPassword.text.toString()
        ).let { re ->
            if (re) create.dismiss()
        }
    }
    create.show()

}

fun Activity.titleDialog(title: String, name: String, callBack: (String) -> Unit) {

    val binding by lazy {
        RenameDialogLayoutBinding.inflate(
            newLayoutInflater,
            root,
            false
        ).apply {
            this.renameTitle.text = title
            renameInput.text = Editable.Factory.getInstance().newEditable(name)
        }
    }

    AlertDialog.Builder(this).also {
        it.setView(binding.root)
    }.create().also { dialog ->
        binding.confirm.setOnClickListener {
            callBack(binding.renameInput.text.toString())
            dialog.dismiss()
        }
        binding.cancel.setOnClickListener {
            dialog.dismiss()
        }
    }.show()
}

fun Activity.cateDialog(
    addCate: () -> Unit,
    addCon: () -> Unit
) {
    val binding by lazy {
        CateDialogLayoutBinding.inflate(
            newLayoutInflater,
            root,
            false
        )
    }

    AlertDialog.Builder(this, R.style.TransparentAlertDialog).setView(binding.root).create()
        .also { dialog ->
            binding.btnAddCate.setOnClickListener {
                dialog.dismiss()
                addCate.invoke()
            }
            binding.btnAddCon.setOnClickListener {
                dialog.dismiss()
                addCon.invoke()
            }
        }.show()

}

fun Activity.checkListDialog(
    title: String,
    dataList: MutableList<String>,
    callBack: ((String?) -> Unit)? = null
) {
    val binding = ListPopWindowsLayoutBinding.inflate(
        newLayoutInflater,
        root,
        false
    )
    val create = AlertDialog.Builder(this).setView(binding.root).create()
    binding.apply {
        listTitle.text = title
        listList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CheckListAdapter(context, dataList)
        }
        listConfirm.setOnClickListener {
            listList.adapter.let {
                if (it is CheckListAdapter)
                    callBack?.invoke(if (it.selectList.size > 0) it.selectList[0] else null)
            }
            create.dismiss()
        }
    }
    create.show()
}

suspend fun Activity.imageListDialog(
    dataList: MutableList<Uri>
) = onResult(Dispatchers.Main) { co ->
    val binding = ImageListDialogLayoutBinding.inflate(newLayoutInflater, root, false)
    val create = AlertDialog.Builder(this@imageListDialog).setView(binding.root).create()
    binding.apply {
        listList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : BaseAdapter<Uri, PhotoCardLayoutBinding, Any>(context) {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): Holder<PhotoCardLayoutBinding> {
                    return Holder(PhotoCardLayoutBinding.inflate(layoutInflater, parent, false))
                }

                override fun onBindViewHolder(
                    holder: Holder<PhotoCardLayoutBinding>,
                    position: Int
                ) {
                    Image.load(mList[position]).with(context).into(holder.binding.photoCardPhoto)
                    val i = position + 1
                    holder.binding.photoCardTitle.text = i.toString()
                }

            }.apply {
                notifyListChangedCO(dataList)
            }
        }
        listConfirm.setOnClickListener {
            co.resumeWith(Result.success(true))
            create.dismiss()
        }
        listDismiss.setOnClickListener {
            co.resumeWith(Result.success(false))
            create.dismiss()
        }
    }
    create.show()
}