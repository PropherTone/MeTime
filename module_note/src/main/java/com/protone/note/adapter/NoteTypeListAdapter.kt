package com.protone.note.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.entity.NoteDir
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.note.databinding.NoteTpyeListAdapterBinding

class NoteTypeListAdapter(
    context: Context,
    block: NoteTypeListAdapterDataProxy.() -> Unit
) : SelectListAdapter<NoteTpyeListAdapterBinding, NoteDir, Any>(context) {

    init {
        NoteTypeListAdapterDataProxy().block()
    }

    override fun setSelect(content: NoteTpyeListAdapterBinding, position: Int, isSelect: Boolean) {
        content.noteTypeSelectGuide.isGone = !isSelect
    }

    override fun itemIndex(path: NoteDir): Int = mList.indexOf(path)

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

    override fun onBindViewHolder(holder: Holder<NoteTpyeListAdapterBinding>, position: Int) {
        setSelect(holder.binding, holder.layoutPosition, mList[holder.layoutPosition] in selectList)
        holder.binding.apply {
            root.setOnClickListener {
                if (mList[holder.layoutPosition] in selectList) return@setOnClickListener
                checkSelect(holder.layoutPosition, mList[holder.layoutPosition])
                onTypeSelected?.invoke(mList[holder.layoutPosition])
            }
            root.setOnLongClickListener {
                AlertDialog.Builder(context).setPositiveButton(
                    context.getString(R.string.confirm)
                ) { dialog, _ ->
                    val noteType = mList[holder.layoutPosition]
                    deleteNoteDir?.invoke(noteType)
                    mList.removeAt(holder.layoutPosition)
                    notifyItemRemovedChecked(holder.layoutPosition)
                    selectDefault()
                    dialog.dismiss()
                }.setNegativeButton(R.string.cancel.getString()) { dialog, _ ->
                    dialog.dismiss()
                }.setTitle(R.string.delete.getString()).create().show()
                return@setOnLongClickListener false
            }
            noteTypeName.text = mList[holder.layoutPosition].name
            noteTypeAddNote.setOnClickListener {
                addNote?.invoke(noteTypeName.text.toString())
            }
        }
    }

    private fun selectDefault() {
        val index = mList.indexOfFirst { it.name == R.string.all.getString() }
        if (index == -1) return
        selectList.clear()
        mList[index].also {
            checkSelect(index, it)
            onTypeSelected?.invoke(it)
        }
    }

    fun insertNoteDir(noteDir: NoteDir) {
        mList.add(noteDir)
        notifyItemInsertedChecked(mList.size)
    }

    fun setNoteTypeList(list: List<NoteDir>) {
        val size = mList.size
        mList.clear()
        notifyItemRangeRemovedChecked(0, size)
        selectList.clear()
        NoteDir(R.string.all.getString(), "").let {
            mList.add(it)
            selectList.add(it)
        }
        mList.addAll(list)
        notifyItemRangeInsertedChecked(0, mList.size)
    }

    private var addNote: ((String?) -> Unit)? = null
    private var onTypeSelected: ((NoteDir) -> Unit)? = null
    private var deleteNoteDir: ((NoteDir) -> Unit)? = null

    inner class NoteTypeListAdapterDataProxy {
        fun addNote(block: (String?) -> Unit) {
            addNote = block
        }

        fun onTypeSelected(block: (NoteDir) -> Unit) {
            onTypeSelected = block
        }

        fun deleteNoteDir(block: (NoteDir) -> Unit) {
            deleteNoteDir = block
        }
    }

}