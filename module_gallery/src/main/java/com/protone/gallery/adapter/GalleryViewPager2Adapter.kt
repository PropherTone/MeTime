package com.protone.gallery.adapter

import android.content.Context
import android.view.ViewGroup
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.view.adapter.BaseAdapter
import com.protone.gallery.databinding.GalleryVp2AdapterLayoutBinding
import kotlinx.coroutines.launch

class GalleryViewPager2Adapter(context: Context,data: MutableList<GalleryMedia>) :
    BaseAdapter<GalleryMedia,GalleryVp2AdapterLayoutBinding, Any>(context) {

    init {
        mList.addAll(data)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<GalleryVp2AdapterLayoutBinding> =
        Holder(GalleryVp2AdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false))

    override fun onBindViewHolder(holder: Holder<GalleryVp2AdapterLayoutBinding>, position: Int) {
        holder.binding.apply {
            if (!mList[position].name.contains("gif")) {
                image.setImageResource(mList[position].uri)
            } else {
                Image.load(mList[position].uri)
                    .with(context)
                    .skipMemoryCache()
                    .into(image)
            }
            image.onSingleTap = {
                launch {
                    onClk?.invoke()
                }
            }
            root.setOnClickListener {
                launch {
                    onClk?.invoke()
                }
            }
        }
    }

    var onClk: (() -> Unit)? = null

}