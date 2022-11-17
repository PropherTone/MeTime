package com.protone.ui.view.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.protone.ui.databinding.LogListLayoutBinding
import com.protone.common.baseType.getFileName
import com.protone.common.context.newLayoutInflater

class LogListAdapter(context: Context) : BaseAdapter<LogListLayoutBinding, Any>(context) {

    private val logs = mutableListOf<String>()
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
            logName.text = logs[position].getFileName()
            logShare.setOnClickListener {
                logEvent?.shareLog(logs[position])
            }
            logWatch.setOnClickListener {
                logEvent?.viewLog(logs[position])
            }
        }
    }

    override fun getItemCount(): Int = logs.size

    fun initLogs(data: MutableList<String>) {
        data.forEach {
            if (!logs.contains(it)) {
                logs.add(it)
                notifyItemInserted(logs.size)
            }
        }
    }

    interface LogEvent {
        fun shareLog(path: String)
        fun viewLog(path: String)
    }
}