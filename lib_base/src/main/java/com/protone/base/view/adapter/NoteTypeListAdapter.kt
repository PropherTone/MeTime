package com.protone.base.view.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.base.R
import com.protone.base.databinding.NoteTpyeListAdapterBinding
import com.protone.common.baseType.getString
import com.protone.common.entity.NoteDir

class NoteTypeListAdapter(
    context: Context,
    private val noteTypeListAdapterDataProxy: NoteTypeListAdapterDataProxy
) : SelectListAdapter<NoteTpyeListAdapterBinding, NoteDir, Any>(context) {

    private val noteDirList = arrayListOf<NoteDir>()

    override val select: (holder: Holder<NoteTpyeListAdapterBinding>, isSelect: Boolean) -> Unit =
        { holder, select ->
            holder.binding.noteTypeSelectGuide.isGone = !select
        }

    override fun itemIndex(path: NoteDir): Int = noteDirList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<NoteTpyeListAdapterBinding> {
        return Holder(
            NoteTpyeListAdapterBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    var addNote: ((String?) -> Unit)? = null
    var onTypeSelected: ((NoteDir) -> Unit)? = null

    override fun onBindViewHolder(holder: Holder<NoteTpyeListAdapterBinding>, position: Int) {
        setSelect(holder, noteDirList[position] in selectList)
        holder.binding.apply {
            root.setOnClickListener {
                if (noteDirList[position] in selectList) return@setOnClickListener
                checkSelect(holder, noteDirList[position])
                onTypeSelected?.invoke(noteDirList[position])
            }
            root.setOnLongClickListener {
                AlertDialog.Builder(context).setPositiveButton(
                    context.getString(R.string.confirm)
                ) { dialog, _ ->
                    val noteType = noteDirList[position]
                    noteTypeListAdapterDataProxy.deleteNoteDir(noteType)
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }.setNegativeButton(R.string.cancel.getString()) { dialog, _ ->
                    dialog.dismiss()
                }.setTitle(R.string.delete.getString()).create().show()
                return@setOnLongClickListener false
            }
            noteTypeName.text = noteDirList[holder.layoutPosition].name
            noteTypeAddNote.setOnClickListener {
                addNote?.invoke(noteTypeName.text.toString())
            }
        }
    }

    override fun getItemCount(): Int = noteDirList.size

    fun insertNoteDir(noteDir: NoteDir) {
        noteDirList.add(noteDir)
        notifyItemInserted(noteDirList.size)
    }

    fun setNoteTypeList(list: List<NoteDir>) {
        val size = noteDirList.size
        noteDirList.clear()
        notifyItemRangeRemoved(0,size)
        selectList.clear()
        NoteDir(R.string.all.getString(), "").let {
            noteDirList.add(it)
            selectList.add(it)
        }
        noteDirList.addAll(list)
        notifyItemRangeInserted(0,noteDirList.size)
    }

    interface NoteTypeListAdapterDataProxy {
        fun deleteNoteDir(noteType: NoteDir)
    }

}