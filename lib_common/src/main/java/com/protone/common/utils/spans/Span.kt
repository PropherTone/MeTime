package com.protone.common.utils.spans

import android.graphics.Color
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan

class ColorSpan(color: Int) : ForegroundColorSpan(color) {
    constructor(color: String) : this(Color.parseColor(color))
}

class BackColorSpan(color: Int) : BackgroundColorSpan(color) {
    constructor(color: String) : this(Color.parseColor(color))
}