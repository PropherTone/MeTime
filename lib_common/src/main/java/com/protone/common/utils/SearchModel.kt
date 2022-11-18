package com.protone.common.utils

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference

class SearchModel(editText: EditText, private val query: SearchModel.() -> Unit) : TextWatcher {

    private var timerHandler: Handler? = Handler(Looper.getMainLooper()) {
        if (it.what == 0) {
            query.invoke(this)
        }
        false
    }

    private var delayed = 500L

    private var input = ""

    private var weakEditText: WeakReference<EditText>? = null

    init {
        WeakReference(editText).also {
            weakEditText = it
        }.get()?.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        timerHandler?.removeCallbacksAndMessages(null)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        timerHandler?.removeCallbacksAndMessages(null)
    }

    override fun afterTextChanged(s: Editable?) {
        input = s.toString()
        timerHandler?.sendEmptyMessageDelayed(0, delayed)
    }

    fun destroy() {
        weakEditText?.clear()
        weakEditText = null
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
    }

    fun getInput() = input
}