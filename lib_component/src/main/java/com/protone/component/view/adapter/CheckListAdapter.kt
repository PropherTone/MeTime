package com.protone.component.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.component.databinding.CheckListAdapterLayoutBinding
import com.protone.common.context.newLayoutInflater

class CheckListAdapter(
    context: Context,
    dataList: MutableList<String>? = null,
    private val check: Boolean = true
) : SelectListAdapter<CheckListAdapterLayoutBinding, String, Any>(
    context
) {

    var dataList: MutableList<String> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    init {
        multiChoose = false
        if (dataList != null) {
            this.dataList.addAll(dataList)
        }
    }

    override val select: (holder: Holder<CheckListAdapterLayoutBinding>, isSelect: Boolean) -> Unit =
        { holder, isSelect ->
            holder.binding.clCheck.isChecked = isSelect
        }

    override fun itemIndex(path: String): Int = dataList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<CheckListAdapterLayoutBinding> =
        Holder(CheckListAdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false))

    override fun onBindViewHolder(holder: Holder<CheckListAdapterLayoutBinding>, position: Int) {
        setSelect(holder,dataList[position] in selectList)
        holder.binding.apply {
            clCheck.isGone = !check
            root.setOnClickListener {
                if (check) {
                    checkSelect(holder, dataList[position])
                } else startNote?.invoke(dataList[position])
            }
            clCheck.isClickable = false
            clName.text = dataList[position]
        }
    }

    var startNote: ((String) -> Unit)? = null

    override fun getItemCount(): Int = dataList.size
}