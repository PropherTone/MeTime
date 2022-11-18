package com.protone.gallery.adapter

import android.content.Context
import android.view.ViewGroup
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.GlideConfigConstant
import com.protone.component.view.adapter.BaseAdapter
import com.protone.gallery.databinding.GalleryVp2AdapterLayoutBinding
import kotlinx.coroutines.launch

class GalleryViewPager2Adapter(context: Context, private val data: MutableList<GalleryMedia>) :
    BaseAdapter<GalleryVp2AdapterLayoutBinding, Any>(context) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<GalleryVp2AdapterLayoutBinding> =
        Holder(GalleryVp2AdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false))

    override fun onBindViewHolder(holder: Holder<GalleryVp2AdapterLayoutBinding>, position: Int) {
        holder.binding.apply {
            if (!data[position].name.contains("gif")) {
                image.setImageResource(data[position].uri)
            } else {
                Image.load(data[position].uri)
                    .addConfig(GlideConfigConstant.SkipMemoryCache)
                    .into(context, image)
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

    override fun getItemCount(): Int = data.size
}