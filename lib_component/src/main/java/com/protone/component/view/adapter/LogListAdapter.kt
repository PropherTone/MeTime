package com.protone.component.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.protone.component.databinding.LogListLayoutBinding
import com.protone.common.baseType.getFileName
import com.protone.common.context.newLayoutInflater

class LogListAdapter(context: Context) : BaseAdapter<String,LogListLayoutBinding, Any>(context) {

    var logEvent: LogEvent? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<LogListLayoutBinding> {
        return Holder(LogListLayoutBinding.inflate(context.newLayoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder<LogListLayoutBinding>, position: Int) {
        holder.binding.apply {
            logName.text = mList[position].getFileName()
            logShare.setOnClickListener {
                logEvent?.shareLog(mList[position])
            }
            logWatch.setOnClickListener {
                logEvent?.viewLog(mList[position])
            }
        }
    }

    fun initLogs(data: MutableList<String>) {
        data.forEach {
            if (!mList.contains(it)) {
                mList.add(it)
                notifyItemInserted(mList.size)
            }
        }
    }

    interface LogEvent {
        fun shareLog(path: String)
        fun viewLog(path: String)
    }
}