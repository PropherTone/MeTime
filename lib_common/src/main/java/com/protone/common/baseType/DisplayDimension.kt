package com.protone.common.baseType

import android.util.TypedValue
import com.protone.common.context.MApplication
import kotlin.math.roundToInt

var DPI = 0

//中文字号    英文字号（磅）X    毫米      像素
// 1英寸        72pt      25.30mm    95.6px
// 大特号       63pt      22.14mm     83.7px
// 特号         54pt      18.97mm     71.7px
// 初号         42pt      14.82mm     56px
// 小初         36pt      12.70mm     48px
// 一号         26pt      9.17mm     34.7px
// 小一         24pt      8.47mm     32px
// 二号         22pt      7.76mm     29.3px
// 小二         18pt      6.35mm     24px
// 三号         16pt      5.64mm     21.3px
// 小三         15pt      5.29mm     20px
// 四号         14pt      4.94mm     18.7px
// 小四         12pt      4.23mm     16px
// 五号         10.5pt    3.70mm     14px
// 小五         9pt       3.18mm     12px
// 六号         7.5pt     2.56mm     10px
// 小六         6.5pt     2.29mm     8.7px
// 七号         5.5pt     1.94mm     7.3px
// 八号         5pt       1.76mm     6.7px

// Px = Pt * DPI / 72
// Pt = Px * 72 / DPI

fun Int.px2Pt() = (this * 72f / DPI).roundToInt()

fun Int.pt2Px() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PT,
    this.toFloat(),
    MApplication.app.resources.displayMetrics
).roundToInt()

fun Int.sp2Px() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    this.toFloat(),
    MApplication.app.resources.displayMetrics
).roundToInt()

fun Int.px2Sp() = (this / MApplication.app.resources.displayMetrics.scaledDensity).roundToInt()
