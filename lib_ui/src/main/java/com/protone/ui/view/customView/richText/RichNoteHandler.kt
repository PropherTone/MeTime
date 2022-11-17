package com.protone.ui.view.customView.richText

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.deleteFile
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.baseType.toBitmap
import com.protone.common.entity.*
import com.protone.common.utils.json.jsonToList
import com.protone.common.utils.json.listToJson
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.common.utils.onResult
import com.protone.common.utils.spans.ISpanForEditor
import com.protone.common.utils.spans.SpanStates
import com.protone.common.utils.spans.setSpan
import com.protone.common.utils.spans.toBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.*

class RichNoteHandler(private val editor: ISpanForEditor) {

    companion object {
        const val TEXT = 0X01
        const val PHOTO = 0X02
        const val MUSIC = 0x03
        const val VIDEO = 0x04
    }

    var curPosition = 0
        private set

    var inIndex = false
        private set

    var imageLoader: IRichNoteImageLoader? = null

    private val deletedMedias by lazy { mutableListOf<String>() }

    fun addDeleteMedias(media: String) = deletedMedias.add(media)

    fun updatePosition(index: Int) {
        if (!inIndex) curPosition = index
    }

    fun getCurrentEditText() = getEditText(curPosition)

    fun getCurrentChild() = editor.getChild(curPosition)

    /**
     * Get current [RichStates]
     *
     * @return [RichStates] or null when caught [IndexOutOfBoundsException]
     */
    fun getCurRichStates(): RichStates? {
        return try {
            val tag = getCurrentChild()?.tag ?: return null
            when (tag) {
                is RichStates -> tag
                else -> null
            }
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    /**
     * Basic method for set span and update [RichNoteStates]
     *
     * @param targetSpan Target span
     */
    fun setEditTextSpan(targetSpan: SpanStates.Spans) {
        getCurrentEditText()?.also {
            var start = it.selectionStart
            var end = it.selectionEnd
            if (start == end) return
            SpanStates(start, end, targetSpan).let { spanStates ->
                if (spanStates.isParagraphSpan()) {
                    val text = it.text
                    start = text.substring(0, start).lastIndexOf("\n") + 1
                    end = text.substring(end).indexOf("\n")
                    if (start == -1) {
                        start = 0
                    }
                    if (end == -1) {
                        end = text.length
                    } else {
                        end += it.selectionEnd
                    }
                    it.setSelection(start, end)
                }
                it.text.setSpan(spanStates, start, end)
            }
            it.tag?.let { rs ->
                if (rs is RichNoteStates) rs.apply { this.text = it.text }
            }
        }
    }

    /**
     * Generate rich text
     *
     * @param list List of [RichStates] use for generating
     */
    suspend fun setRichList(list: List<RichStates>) {
        editor.apply {
            inIndex = true
            list.forEach {
                when (it) {
                    is RichNoteStates -> insertText(it)
                    is RichVideoStates -> insertVideo(it)
                    is RichPhotoStates -> insertImage(it, it.uri.toBitmap())
                    is RichMusicStates -> insertMusic(it)
                }
                curPosition++
            }
            inIndex = false
        }
    }

    suspend fun setRichList(richCode: Int, text: String) {
        var code = richCode
        val statesStrings = text.jsonToList(String::class.java)
        var listSize = statesStrings.size - 1
        val richList = arrayListOf<RichStates>()
        while (code > 0) {
            richList.add(
                when (code % 10) {
                    PHOTO -> {
                        statesStrings[listSize--].toEntity(RichPhotoStates::class.java)
                    }
                    MUSIC -> {
                        statesStrings[listSize--].toEntity(RichMusicStates::class.java)
                    }
                    VIDEO -> {
                        statesStrings[listSize--].toEntity(RichVideoStates::class.java)
                    }
                    else -> {
                        val richNoteSer =
                            statesStrings[listSize--].toEntity(RichNoteSer::class.java)
                        RichNoteStates(richNoteSer.text)
                    }
                }
            )
            code /= 10
        }
        setRichList(richList.reversed())
    }

    /**
     * Indexing rich note for database to store
     *
     * @return [Pair]
     * <[Int],[String]>
     *
     * [Int] : A sort of number that mark the [RichStates] from left to right
     *
     * [String] : Json of [RichStates]>
     */
    suspend fun indexRichNote(
        title: String,
        childCount: Int,
        onSaveResult: suspend (ArrayList<Uri>) -> Boolean
    ): Pair<Int, String> {
        editor.apply {
            val richArray = arrayOfNulls<String>(childCount)
            val richSer = arrayListOf<String>()
            val reList = arrayListOf<Uri>()
            val taskChannel = Channel<Pair<String, Int>>(Channel.UNLIMITED)
            var richStates = 0
            var count = 0
            return onResult(Dispatchers.Default) {
                async(Dispatchers.IO) {
                    deletedMedias.forEach {
                        it.deleteFile()
                    }
                }.start()
                async(Dispatchers.Default) {
                    while (isActive) {
                        taskChannel.receiveAsFlow().bufferCollect {
                            if (it.second < childCount) {
                                richArray[it.second] = it.first
                                count++
                            }
                            if (count >= childCount) {
                                taskChannel.close()
                            }
                        }
                        if (withContext(Dispatchers.Main) {
                                onSaveResult.invoke(reList)
                            }) {
                            richArray.onEach {
                                it?.let { state ->
                                    richSer.add(state)
                                }
                            }
                            it.resumeWith(
                                Result.success(
                                    Pair(
                                        richStates,
                                        richSer.listToJson(String::class.java)
                                    )
                                )
                            )
                        }
                        break
                    }
                }.start()
                for (i in 0 until childCount) {
                    val view = getChild(i)
                    if (view != null) when (val tag = view.tag) {
                        is RichNoteStates -> {
                            richStates = richStates * 10 + TEXT
                            taskChannel.trySend(
                                Pair(
                                    RichNoteSer(
                                        (view as EditText).text?.toBase64() ?: "",
                                        ""
                                    ).toJson(), i
                                )
                            )
                        }
                        is RichVideoStates -> {
                            richStates = richStates * 10 + VIDEO
                            taskChannel.trySend(Pair(tag.toJson(), i))
                        }
                        is RichPhotoStates -> {
                            richStates = richStates * 10 + PHOTO
                            if (tag.path != null) {
                                taskChannel.trySend(Pair(tag.toJson(), i))
                            } else async(Dispatchers.IO) {
                                tag.uri.imageSaveToDisk(
                                    tag.name + "_${title}",
                                    dir = title,
                                    w = view.measuredWidth,
                                    h = view.measuredHeight
                                )?.let {
                                    if (tag.path == null) {
                                        tag.path = it
                                    } else {
                                        reList.add(tag.uri)
                                    }
                                }
                                taskChannel.trySend(Pair(tag.toJson(), i))
                            }.start()
                        }
                        is RichMusicStates -> {
                            richStates = richStates * 10 + MUSIC
                            taskChannel.trySend(Pair(tag.toJson(), i))
                        }
                    }
                }

            }

        }
    }

    /**
     * Get edittext
     *
     * @param position target
     * @return [EditText] or null when target is not [EditText] or caught [IndexOutOfBoundsException]
     */
    private fun getEditText(position: Int): EditText? {
        return try {
            editor.getChild(position).let {
                if (it is EditText) it else null
            }
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }
}

interface IRichNoteImageLoader {
    fun loadImage(context: Context, uri: Uri?, view: ImageView)
    fun loadImage(context: Context, path: String?, view: ImageView)
    fun loadImage(context: Context, bitmap: Bitmap?, view: ImageView)
    fun loadError(context: Context, view: ImageView)
}

interface MyTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {}
}