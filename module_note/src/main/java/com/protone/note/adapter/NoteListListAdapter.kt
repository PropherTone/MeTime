package com.protone.note.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.protone.common.baseType.toDateString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Note
import com.protone.component.view.adapter.BaseListAdapter
import com.protone.note.databinding.NoteListAdapterLayoutBinding

class NoteListListAdapter(
    context: Context,
    private val glideLoader: RequestBuilder<Drawable>,
    block: NoteListEvent.() -> Unit
) : BaseListAdapter<Note, NoteListAdapterLayoutBinding, Any>(
    context,
    false,
    AsyncDifferConfig.Builder(object :
        DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
            oldItem.title == newItem.title && oldItem.time == newItem.time

    }).build()
) {

    init {
        NoteListEvent().block()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<NoteListAdapterLayoutBinding> {
        val binding = NoteListAdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<NoteListAdapterLayoutBinding>, position: Int) {
        holder.binding.apply {
            noteBack.setOnClickListener {
                onNote?.invoke(currentList[holder.layoutPosition].title)
            }
            noteBack.setOnLongClickListener {
                onDelete?.invoke(currentList[holder.layoutPosition])
                true
            }
            currentList[holder.layoutPosition].let {
                glideLoader.load(it.imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(noteBack)
                noteTitle.text = it.title
                noteDate.text = it.time.toDateString()
            }
        }
    }

    private var onNote: ((title: String) -> Unit)? = null
    private var onDelete: ((note: Note) -> Unit)? = null

    inner class NoteListEvent {

        fun onNote(block: (title: String) -> Unit) {
            onNote = block
        }

        fun onDelete(block: (note: Note) -> Unit) {
            onDelete = block
        }
    }
}