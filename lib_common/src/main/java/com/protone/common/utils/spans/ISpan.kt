package com.protone.common.utils.spans

interface ISpan {
    fun setBold()
    fun setItalic()
    fun setUnderlined()
    fun setStrikethrough()
    fun setURL(url: String)
    fun setSubscript()
    fun setSuperscript()
}