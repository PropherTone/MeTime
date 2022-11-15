//package com.protone.ui.adapter
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import android.graphics.Typeface
//import android.net.Uri
//import android.os.Handler
//import android.os.Message
//import android.text.Editable
//import android.text.Spanned
//import android.text.TextWatcher
//import android.text.style.AbsoluteSizeSpan
//import android.text.style.StrikethroughSpan
//import android.text.style.StyleSpan
//import android.text.style.UnderlineSpan
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageView
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.protone.api.TAG
//import com.protone.api.baseType.toMediaBitmapByteArray
//import com.protone.api.context.newLayoutInflater
//import com.protone.api.entity.*
//import com.protone.api.json.listToJson
//import com.protone.api.json.toJson
//import com.protone.base.view.customView.InRecyclerView
//import com.protone.ui.databinding.*
//import com.protone.api.spans.ISpanForEditor
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.withContext
//
//class RichNoteAdapter(
//    val context: Context,
//    private val isEditable: Boolean = true,
//    dataList: ArrayList<Any>
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ISpanForEditor {
//
//    private val dataList by lazy { arrayListOf<Any>() }
//
//    private var curPosition: Int? = null
//
//    private var parent: InRecyclerView? = null
//
//    companion object {
//        const val TEXT = 0X01
//        const val PHOTO = 0X02
//        const val MUSIC = 0x03
//        const val VIDEO = 0x04
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private val handler = Handler(context.mainLooper) {
//        Log.d(TAG, "what: ${it.what}")
//        when (it.what) {
//            1 -> notifyItemInserted(it.arg1)
//            2 -> notifyItemRemoved(it.arg1)
//            3 -> notifyItemRangeRemoved(it.arg1, it.arg2)
//            4 -> notifyDataSetChanged()
//        }
//        val index = dataList.size.let { size ->
//            var x = 0
//            for (i in 0 until size) {
//                x = i
//                if (getItemViewType(i) == TEXT) break
//            }
//            x
//        }
//        getCurEditText(index)?.requestFocus()
//        false
//    }
//
//    init {
//        this.dataList.addAll(dataList)
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        parent = recyclerView as InRecyclerView
//        parent?.setOnInterceptTouchEvent(object : InRecyclerView.OnTouch {
//            override fun onTouch(x: Float, y: Float) {
//                curPosition = parent?.findChildViewUnder(x, y)?.let {
//                    parent?.getChildViewHolder(it)?.adapterPosition
//                }
//            }
//        })
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        when (dataList[position]) {
//            is RichNoteStates -> return TEXT
//            is RichVideoStates -> return VIDEO
//            is RichPhotoStates -> return PHOTO
//            is RichMusicStates -> return MUSIC
//        }
//        return TEXT
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
//        when (viewType) {
//            PHOTO -> RichPhotoHolder(
//                RichPhotoLayoutBinding.inflate(
//                    context.newLayoutInflater,
//                    parent,
//                    false
//                )
//            )
//            MUSIC -> RichMusicHolder(
//                RichMusicLayoutBinding.inflate(
//                    (context.newLayoutInflater),
//                    parent,
//                    false
//                )
//            )
//            VIDEO -> RichVideoHolder(
//                RichVideoLayoutBinding.inflate(
//                    context.newLayoutInflater,
//                    parent,
//                    false
//                )
//            )
//            else -> if (isEditable) RichEditTextHolder(
//                RichEditTextLayoutBinding.inflate(
//                    context.newLayoutInflater,
//                    parent,
//                    false
//                )
//            ) else RichTextHolder(
//                RichTextLayoutBinding.inflate(
//                    context.newLayoutInflater,
//                    parent,
//                    false
//                )
//            )
//        }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (holder) {
//            is RichTextHolder -> {
//                holder.binding.text.text = (dataList[holder.layoutPosition] as RichNoteStates).text
//            }
//            is RichEditTextHolder -> {
//                holder.binding.edit.apply {
//                    setText((dataList[holder.layoutPosition] as RichNoteStates).text)
//                    addTextChangedListener(object : TextWatcher {
//                        override fun beforeTextChanged(
//                            s: CharSequence?,
//                            start: Int,
//                            count: Int,
//                            after: Int
//                        ) = Unit
//
//                        override fun onTextChanged(
//                            s: CharSequence?,
//                            start: Int,
//                            before: Int,
//                            count: Int
//                        ) = Unit
//
//                        override fun afterTextChanged(s: Editable?) {
//                            val layoutPosition = holder.layoutPosition
//                            if (s?.length != 0) {
//                                (dataList[layoutPosition] as RichNoteStates).text = s
//                            } else if (s.isEmpty() && layoutPosition != 0 && layoutPosition < dataList.size) {
//                                dataList.removeAt(layoutPosition)
//                                if (getItemViewType(layoutPosition - 1) != TEXT) {
//                                    dataList.removeAt(layoutPosition - 1)
//                                }
//                                handleItemRangeRemoved(layoutPosition - 1, layoutPosition)
//                            }
//                        }
//
//                    })
//                }
//            }
//            is RichPhotoHolder -> {
//                holder.binding.apply {
//                    (dataList[holder.layoutPosition]
//                            as RichPhotoStates).let {
//                        richPhotoTitle.text = it.name
//                        it.date?.let { d -> richPhotoDetail.text = d }
//                        richPhotoTvContainer.setOnClickListener { c ->
//                            c.visibility = if (c.isVisible) View.INVISIBLE else View.VISIBLE
//                        }
//                        glideIv(richPhotoIv, it.uri)
//                    }
//                }
//            }
//            is RichMusicHolder -> {
//
//            }
//            is RichVideoHolder -> {
//
//            }
//        }
//    }
//
//    override fun getItemCount(): Int = dataList.size
//
//    private fun glideIv(iv: ImageView, uri: Uri) {
//        val toMediaBitmapByteArray = uri.toMediaBitmapByteArray()
//        Glide.with(context).load(toMediaBitmapByteArray).into(iv)
//    }
//
//    class RichTextHolder(val binding: RichTextLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    class RichEditTextHolder(val binding: RichEditTextLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    class RichPhotoHolder(val binding: RichPhotoLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    class RichMusicHolder(val binding: RichMusicLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    class RichVideoHolder(val binding: RichVideoLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun setBold() {
//        setEditTextSpan(StyleSpan(Typeface.BOLD), SpanStates.Spans.StyleSpan, style = Typeface.BOLD)
//    }
//
//    override fun setItalic() {
//        setEditTextSpan(
//            StyleSpan(Typeface.ITALIC),
//            SpanStates.Spans.StyleSpan,
//            style = Typeface.ITALIC
//        )
//    }
//
//    override fun setSize(size: Int) {
//        setEditTextSpan(
//            AbsoluteSizeSpan(size),
//            SpanStates.Spans.AbsoluteSizeSpan,
//            absoluteSize = size
//        )
//    }
//
//    override fun setUnderlined() {
//        setEditTextSpan(UnderlineSpan(), SpanStates.Spans.UnderlineSpan)
//    }
//
//    override fun setStrikethrough() {
//        setEditTextSpan(StrikethroughSpan(), SpanStates.Spans.StrikeThroughSpan)
//    }
//
//    override fun setColor(color: Any) {
//        setEditTextSpan(
//            when (color) {
//                is Int -> ColorSpan(color)
//                is String -> ColorSpan(color)
//                else -> ColorSpan(Color.BLACK)
//            }, SpanStates.Spans.ForegroundColorSpan, iColor = color
//        )
//    }
//
//    override fun insertImage(photo: RichPhotoStates) {
//        TODO("Not yet implemented")
//    }
//
//    override fun insertVideo(video: RichVideoStates) {
//        TODO("Not yet implemented")
//    }
//
//    override fun insertMusic(music: RichMusicStates) {
//        TODO("Not yet implemented")
//    }
//
//    fun setImage(uri: Uri, link: String?, name: String, date: String?) {
//        getCurEditText(curPosition)?.run {
//            val cs = text as CharSequence
//            val sequenceStart = cs.subSequence(0, selectionStart)
//            val sequenceEnd = cs.subSequence(selectionStart, cs.length)
//            curPosition = curPosition?.let {
//                var position = it
//                dataList.removeAt(position)
//                dataList.add(position++, RichNoteStates(sequenceStart, arrayListOf()))
////                dataList.add(position++, RichPhotoStates(uri, link, name, date))
//                dataList.add(position, RichNoteStates(sequenceEnd, arrayListOf()))
//                handleDataSetChanged()
//                getCurEditText(position)?.requestFocus()
//                position
//            }
//        }
//    }
//
//    private fun setEditTextSpan(
//        span: Any,
//        targetSpan: SpanStates.Spans,
//        iColor: Any? = null,
//        absoluteSize: Int? = null,
//        relativeSize: Float? = null,
//        scaleX: Float? = null,
//        style: Int? = null,
//        url: String? = null
//    ) {
//        curPosition?.let { p ->
//            getCurEditText(p)?.also {
//                it.text.setSpan(
//                    span,
//                    it.selectionStart,
//                    it.selectionEnd,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                )
//                when (dataList[p]) {
//                    is RichNoteStates -> (dataList[p] as RichNoteStates).apply {
//                        this.text = it.text
//                        (spanStates as ArrayList)
//                            .add(
//                                SpanStates(
//                                    it.selectionStart,
//                                    it.selectionEnd,
//                                    targetSpan,
//                                    iColor,
//                                    absoluteSize,
//                                    relativeSize,
//                                    scaleX,
//                                    style,
//                                    url
//                                )
//                            )
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getCurEditText(position: Int?): EditText? {
//        return try {
//            (position?.let { parent?.layoutManager?.findViewByPosition(it) } as EditText)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    suspend fun indexRichNote() = withContext(Dispatchers.IO) {
//        suspendCancellableCoroutine<Unit> {
//            val richSer = arrayListOf<String>()
//            var richStates = 0
//            dataList.forEach {
//                when (it) {
//                    is RichNoteStates -> richSer.add(
//                        RichNoteSer(
//                            getCurEditText(dataList.indexOf(it))?.text.toString(),
//                            it.spanStates.listToJson(SpanStates::class.java)
//                        ).toJson()
//                    ).apply { richStates = richStates * 10 + TEXT }
//                    is RichVideoStates -> richSer.add(it.toJson())
//                        .apply { richStates = richStates * 10 + VIDEO }
//                    is RichPhotoStates -> richSer.add(it.toJson())
//                        .apply { richStates = richStates * 10 + PHOTO }
//                    is RichMusicStates -> richSer.add(it.toJson())
//                        .apply { richStates = richStates * 10 + MUSIC }
//                }
//            }
//        }
//    }
//
//    private fun handleItemInserted(position: Int) {
//        handler.sendMessage(Message().apply {
//            what = 1
//            arg1 = position
//        })
//    }
//
//    private fun handleItemRemoved(position: Int) {
//        handler.sendMessage(Message().apply {
//            what = 2
//            arg1 = position
//        })
//    }
//
//    private fun handleItemRangeRemoved(start: Int, end: Int) {
//        handler.sendMessage(Message().apply {
//            what = 3
//            arg1 = start
//            arg2 = end
//        })
//    }
//
//    private fun handleDataSetChanged() {
//        handler.sendMessage(Message().apply {
//            what = 4
//        })
//    }
//
//}