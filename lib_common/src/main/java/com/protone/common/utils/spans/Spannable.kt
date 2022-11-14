package com.protone.common.utils.spans

import android.os.Parcel
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Base64
import com.protone.common.entity.SpanStyle

fun Spannable.setSpan(spanStates: SpanStates, start: Int, end: Int) {
    val spans = spanStates.getTargetSpan()?.let { targetSpan ->
        val getSpans = this.getSpans(
            start,
            end,
            targetSpan.javaClass
        )
        if (targetSpan is StyleSpan) {
            getSpans.filter { charStyle -> (charStyle as StyleSpan).style == targetSpan.style }
                .toTypedArray()
        } else getSpans
    }
    val isCancellableSpan = spanStates.isCancellableSpan()
    val styleList = mutableListOf<SpanStyle>()
    if (spans?.isNotEmpty() == true) {
        fun addStyle(start: Int, end: Int) {
            spanStates.getTargetSpan()?.let { style ->
                styleList.add(SpanStyle(style, start, end))
            }
        }
        spans.forEach { eachSpan ->
            val spanStart = this.getSpanStart(eachSpan)
            val spanEnd = this.getSpanEnd(eachSpan)
            when {
                end in spanStart..spanEnd -> {
                    if (start > spanStart && spanStart != start) {
                        addStyle(spanStart, start)
                    }
                    if (end != spanEnd) {
                        addStyle(end, spanEnd)
                    }
                }
                start in spanStart..spanEnd -> {
                    if (end < spanEnd && end != spanEnd) {
                        addStyle(end, spanEnd)
                    }
                    if (spanStart != start) {
                        addStyle(spanStart, start)
                    }
                }
            }
            this.removeSpan(eachSpan)
        }
    }
    if (spans?.isNotEmpty() == false || isCancellableSpan) {
        spanStates.getTargetSpan()?.let { style ->
            styleList.add(SpanStyle(style, start, end))
        }
    }
    styleList.forEach { spanStyle ->
        this.setSpan(
            spanStyle.span,
            spanStyle.start,
            spanStyle.end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun CharSequence.toBase64(): String {
    val parcel = Parcel.obtain()
    return try {
        TextUtils.writeToParcel(this, parcel, 0)
        val marshall = parcel.marshall()
        String(Base64.encode(marshall, Base64.DEFAULT))
    } catch (e: Exception) {
        ""
    } finally {
        parcel.recycle()
    }
}

fun CharSequence.base64ToCharSequence(): CharSequence? {
    val parcel = Parcel.obtain()
    return try {
        val bytes = Base64.decode(this.toString(), Base64.DEFAULT)
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel)
    } catch (e: Exception) {
        ""
    } finally {
        parcel.recycle()
    }
}

fun CharSequence.indexSpan(spans: List<SpanStates>): CharSequence {
    val str = when (this) {
        is Spannable -> this
        else -> SpannableString(this)
    }
    spans.forEach {
        str.apply {
            setSpan(
                it.getTargetSpan(),
                it.start,
                it.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    return str
}