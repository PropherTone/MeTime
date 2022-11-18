package com.protone.note.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.protone.component.databinding.NoteListAdapterLayoutBinding
import com.protone.common.baseType.toDateString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Note
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.GlideConfigConstant
import com.protone.component.adapter.BaseListAdapter
import com.protone.note.databinding.NoteListAdapterLayoutBinding
import kotlinx.coroutines.launch

class NoteListListAdapter(context: Context) :
    BaseListAdapter<Note, NoteListAdapterLayoutBinding, NoteListListAdapter.NoteEvent>(
        context,
        true,
        AsyncDifferConfig.Builder(object :
            DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem.title == newItem.title && oldItem.time == newItem.time

        }).build()
    ) {

    private val noteList = arrayListOf<Note>()

    sealed class NoteEvent {
        data class NoteDelete(val note: Note) : NoteEvent()
        data class NoteInsert(val note: Note) : NoteEvent()
        data class NoteUpdate(val note: Note) : NoteEvent()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override suspend fun onEventIO(data: NoteEvent) {
        when (data) {
            is NoteEvent.NoteDelete -> {
                val indexOf = noteList.indexOf(data.note)
                if (indexOf != -1) {
                    noteList.removeAt(indexOf)
                    notifyItemRemovedCO(indexOf)
                }
            }
            is NoteEvent.NoteInsert -> {
                noteList.add(0, data.note)
                notifyItemInsertedCO(0)
            }
            is NoteEvent.NoteUpdate -> {
                val index = noteList.indexOf(data.note)
                if (index != -1) {
                    noteList[index] = data.note
                    notifyItemChangedCO(index)
                }
            }
        }
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
            root.setOnClickListener {
                noteListEventListener?.onNote(currentList[holder.layoutPosition].title)
            }
            root.setOnLongClickListener {
                noteListEventListener?.onDelete(currentList[holder.layoutPosition])
                true
            }
            currentList[holder.layoutPosition].let {
                Image.load(it.imagePath)
                    .addConfig(GlideConfigConstant.diskCacheStrategy(GlideConfigConstant.DiskCacheStrategy.NONE))
                    .into(context, noteBack)
                noteTitle.text = it.title
                noteDate.text = it.time.toDateString()
            }
        }
    }

    fun setNoteList(list: List<Note>) {
        val size = noteList.size
        noteList.clear()
        notifyItemRangeRemoved(0, size)
        noteList.addAll(list)
        notifyItemRangeInserted(0, noteList.size)
    }

    fun deleteNote(note: Note) {
        launch {
            emit(NoteEvent.NoteDelete(note))
        }
    }

    fun insertNote(note: Note) {
        launch {
            emit(NoteEvent.NoteInsert(note))
        }
    }

    fun updateNote(note: Note) {
        launch {
            emit(NoteEvent.NoteUpdate(note))
        }
    }

    var noteListEventListener: NoteListEvent? = null

    interface NoteListEvent {
        fun onNote(title: String)
        fun onDelete(note: Note)
    }
}