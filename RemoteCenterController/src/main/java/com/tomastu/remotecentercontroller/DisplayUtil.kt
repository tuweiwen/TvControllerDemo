package com.tomastu.remotecentercontroller

import android.content.Context

fun Float.px2dp(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return this / scale
}

fun Float.dp2px(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return this * scale
}

fun Float.sp2px(context: Context): Float {
    val scale = context.resources.displayMetrics.scaledDensity
    return this * scale
}