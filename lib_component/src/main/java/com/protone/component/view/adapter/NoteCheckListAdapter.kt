package com.protone.component.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.common.context.newLayoutInflater
import com.protone.component.databinding.CheckListAdapterLayoutBinding

class NoteCheckListAdapter(
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
        setAdapterDiff(getDefaultDiff())
    }

    override fun setSelect(
        content: CheckListAdapterLayoutBinding,
        position: Int,
        isSelect: Boolean
    ) {
        content.clCheck.isChecked = isSelect
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
                val layoutPosition = holder.layoutPosition
                if (check) {
                    checkSelect(layoutPosition, mList[layoutPosition])
                } else startNote?.invoke(mList[layoutPosition])
            }
            clCheck.isClickable = false
            clName.text = mList[position]
        }
    }

    var startNote: ((String) -> Unit)? = null

}