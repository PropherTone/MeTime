package com.protone.metime.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.R
import com.protone.component.databinding.*
import com.protone.common.baseType.toDateString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Note
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.component.view.adapter.BaseAdapter
import com.protone.metime.databinding.MusicCardBinding
import com.protone.metime.databinding.NoteCardBinding
import com.protone.metime.databinding.PhotoCardBinding
import com.protone.metime.databinding.VideoCardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainModelListAdapter(
    context: Context,
    private val mainModelListAdapterDataProxy: MainModelListAdapterDataProxy
) : BaseAdapter<ViewDataBinding, RecyclerView.ViewHolder>(context, false) {

    private val itemList = mutableListOf<String>()

    companion object {
        const val TIME = 0x1F
        const val MUSIC = 0x2F
        const val PHOTO = 0x3F
        const val VIDEO = 0x4F
        const val NOTE = 0x5F
        const val UNKNOWN = 0x6F
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position].substring(0, 5)) {
            "tTime" -> TIME
            "music" -> MUSIC
            "photo" -> PHOTO
            "video" -> VIDEO
            "tNote" -> NOTE
            else -> UNKNOWN
        }
    }

    suspend fun loadDataBelow() {
        mainModelListAdapterDataProxy.run {
            photoInTodayJson()?.let {
                itemList.add("photo:${it}")
                withContext(Dispatchers.Main) {
                    notifyItemInserted(itemList.size - 1)
                }
            }
            videoInTodayJson()?.let {
                itemList.add("video:${it}")
                withContext(Dispatchers.Main) {
                    notifyItemInserted(itemList.size - 1)
                }
            }
            randomNoteJson()?.let {
                itemList.add("tNote:${it}")
                withContext(Dispatchers.Main) {
                    notifyItemInserted(itemList.size - 1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<ViewDataBinding> {
        return Holder(
            when (viewType) {
                MUSIC -> MusicCardBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                )
                NOTE -> NoteCardBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                )
                VIDEO -> VideoCardBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                )
                PHOTO -> PhotoCardBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                )
                TIME -> DateLayoutBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                )
                else -> DateLayoutBinding.inflate(
                    context.newLayoutInflater,
                    parent,
                    false
                ).apply { modelTime.text = context.getString(R.string.bruh) }
            }
        )
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        when (holder.binding) {
            is MusicCardBinding -> {
//                val music = itemList[position].substring(6).toEntity(Music::class.java)
            }
            is NoteCardBinding -> {
                val note = itemList[position].substring(6).toEntity(Note::class.java)
                Image.load(note.imagePath).with(context).into((holder.binding as NoteCardBinding).modelNoteIcon)
                (holder.binding as NoteCardBinding).modelNoteTitle.text = note.title
            }
            is PhotoCardBinding -> {
                val media =
                    itemList[position].substring(6).toEntity(GalleryMedia::class.java)
                (holder.binding as PhotoCardBinding).photoCard.apply {
                    title = media.date.toDateString("yyyy/MM/dd E").toString()
                    Image.load(media.uri).with(context).into(photo)
                    setOnClickListener {
                        modelClkListener?.onPhoto(media.toJson())
                    }
                }
            }
            is VideoCardBinding -> {
                val media =
                    itemList[position].substring(6).toEntity(GalleryMedia::class.java)
                (holder.binding as VideoCardBinding).videoPlayer.apply {
                    setVideoPath(media.uri)
                    setFullScreen {
                        modelClkListener?.onVideo(media.toJson())
                    }
                }
                (holder.binding as VideoCardBinding).videoCardTitle.text =
                    media.date.toDateString("yyyy/MM/dd E").toString()
            }
            is DateLayoutBinding -> (holder.binding as DateLayoutBinding).modelTime.text =
                itemList[position].substring(6)
        }
    }

    var modelClkListener: ModelClk? = null

    interface ModelClk {
        fun onPhoto(json: String)
        fun onNote(json: String)
        fun onVideo(json: String)
        fun onMusic(json: String)
    }

    override fun getItemCount(): Int = itemList.size

    interface MainModelListAdapterDataProxy {
        fun photoInTodayJson(): String?
        fun videoInTodayJson(): String?
        fun randomNoteJson(): String?
    }

}