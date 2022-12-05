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

    override val select: (NoteTpyeListAdapterBinding, Int, isSelect: Boolean) -> Unit =
        { binding, _, select ->
            binding.noteTypeSelectGuide.isGone = !select
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
        setSelect(holder.binding, position, mList[position] in selectList)
        holder.binding.apply {
            root.setOnClickListener {
                if (mList[position] in selectList) return@setOnClickListener
                checkSelect(position, mList[position])
                onTypeSelected?.invoke(mList[position])
            }
            root.setOnLongClickListener {
                AlertDialog.Builder(context).setPositiveButton(
                    context.getString(R.string.confirm)
                ) { dialog, _ ->
                    val noteType = mList[position]
                    deleteNoteDir?.invoke(noteType)
                    notifyItemRemoved(position)
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

    fun insertNoteDir(noteDir: NoteDir) {
        mList.add(noteDir)
        notifyItemInserted(mList.size)
    }

    fun setNoteTypeList(list: List<NoteDir>) {
        val size = mList.size
        mList.clear()
        notifyItemRangeRemoved(0, size)
        selectList.clear()
        NoteDir(R.string.all.getString(), "").let {
            mList.add(it)
            selectList.add(it)
        }
        mList.addAll(list)
        notifyItemRangeInserted(0, mList.size)
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