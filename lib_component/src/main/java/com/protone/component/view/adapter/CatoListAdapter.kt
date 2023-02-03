package com.protone.component.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.protone.component.databinding.ImageCateLayoutBinding
import com.protone.component.databinding.TextCateLayoutBinding
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import kotlinx.coroutines.launch

class CatoListAdapter(context: Context, private val catoListDataProxy: CatoListDataProxy) :
    BaseAdapter<String, ViewDataBinding, Any>(context) {

    private var itemClick: ((String) -> Unit)? = null

    init {
        setAdapterDiff(getDefaultDiff())
    }

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].contains("content://")) 1 else 0
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
                        catoListDataProxy.getMedia(mList[position])?.let { media ->
                            Image.load(media.uri).with(context).into(catoBack)
                            catoName.text = media.name
                            root.setOnClickListener {
                                itemClick?.invoke(mList[position])
                            }
                        }
                    }
                is TextCateLayoutBinding -> cato.text = mList[position]
            }
        }
    }

    fun setItemClick(itemClick: (String) -> Unit) {
        this.itemClick = itemClick
    }

    fun refresh(cateList: List<String>?) {
        cateList?.let {
            notifyListChangedCO(it)
        }
    }

    interface CatoListDataProxy {
        suspend fun getMedia(cate: String): GalleryMedia?
    }

}