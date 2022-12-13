package com.protone.component.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryBucket
import com.protone.component.databinding.CheckListAdapterLayoutBinding

class CheckListAdapter(
    context: Context,
    dataList: List<String>? = null,
    private val check: Boolean = true
) : SelectListAdapter<CheckListAdapterLayoutBinding, String, Any>(
    context
) {

    init {
        multiChoose = false
        if (dataList != null) {
            this.mList.addAll(dataList)
        }
    }

    override val select: (CheckListAdapterLayoutBinding, Int, isSelect: Boolean) -> Unit =
        { binding, _, isSelect ->
            binding.clCheck.isChecked = isSelect
        }

    override fun itemIndex(path: String): Int = mList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<CheckListAdapterLayoutBinding> =
        Holder(CheckListAdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false))

    override fun onBindViewHolder(holder: Holder<CheckListAdapterLayoutBinding>, position: Int) {
        setSelect(holder.binding, position, mList[position] in selectList)
        holder.binding.apply {
            clCheck.isGone = !check
            root.setOnClickListener {
                if (check) {
                    checkSelect(position, mList[position])
                } else startNote?.invoke(mList[position])
            }
            clCheck.isClickable = false
            clName.text = mList[position]
        }
    }

    var startNote: ((String) -> Unit)? = null

}