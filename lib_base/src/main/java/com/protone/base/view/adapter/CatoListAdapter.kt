package com.protone.base.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.protone.base.databinding.ImageCateLayoutBinding
import com.protone.base.databinding.TextCateLayoutBinding
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import kotlinx.coroutines.launch

class CatoListAdapter(context: Context, private val catoListDataProxy: CatoListDataProxy) :
    BaseAdapter<ViewDataBinding, String>(context) {

    private val catoList = mutableListOf<String>()
    private var itemClick: ((String) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (catoList[position].contains("content://")) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<ViewDataBinding> =
        Holder(
            when (viewType) {
                1 -> ImageCateLayoutBinding.inflate(context.newLayoutInflater, parent, false)
                else -> TextCateLayoutBinding.inflate(context.newLayoutInflater, parent, false)
            }
        )


    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        holder.binding.apply {
            when (this) {
                is ImageCateLayoutBinding ->
                    launch {
                        catoListDataProxy.getMedia(catoList[position])?.let { media ->
                            Glide.with(context).asDrawable().load(media.uri).into(catoBack)
                            catoName.text = media.name
                            root.setOnClickListener {
                                itemClick?.invoke(catoList[position])
                            }
                        }
                    }
                is TextCateLayoutBinding -> cato.text = catoList[position]
            }
        }
    }

    override fun getItemCount(): Int = catoList.size

    fun setItemClick(itemClick: (String) -> Unit) {
        this.itemClick = itemClick
    }

    fun refresh(cateList : Collection<String>?){
        if (cateList == null) return
        if (catoList.containsAll(cateList)) return
        val size = catoList.size
        catoList.clear()
        notifyItemRangeRemoved(0, size)
        catoList.addAll(cateList)
        notifyItemRangeInserted(0, catoList.size)
    }

    interface CatoListDataProxy {
        suspend fun getMedia(cate: String): GalleryMedia?
    }

}