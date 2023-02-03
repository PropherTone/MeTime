package com.protone.gallery.adapter

import android.content.Context
import android.view.ViewGroup
import com.protone.common.context.newLayoutInflater
import com.protone.component.databinding.TextCateLayoutBinding
import com.protone.component.view.adapter.BaseAdapter

class NoteLinkListAdapter(
    context: Context,
    dataList: List<String>? = null
) : BaseAdapter<String, TextCateLayoutBinding, Any>(context) {

    init {
        if (dataList != null) {
            this.mList.addAll(dataList)
        }
        setAdapterDiff(getDefaultDiff())
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<TextCateLayoutBinding> =
        Holder(TextCateLayoutBinding.inflate(context.newLayoutInflater, parent, false))

    override fun onBindViewHolder(holder: Holder<TextCateLayoutBinding>, position: Int) {
        holder.binding.apply {
            cato.text = mList[position]
            root.setOnClickListener { startNote?.invoke(mList[holder.layoutPosition]) }
        }
    }

    var startNote: ((String) -> Unit)? = null

}