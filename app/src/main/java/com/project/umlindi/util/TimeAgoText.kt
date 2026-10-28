package com.project.umlindi.util

import android.content.Context
import com.project.umlindi.R

fun TimeAgo.toText(context: Context): String = when (this) {
    TimeAgo.Unknown -> ""
    TimeAgo.JustNow -> context.getString(R.string.time_just_now)
    is TimeAgo.Minutes -> context.getString(R.string.time_minutes_ago, count.toInt())
    is TimeAgo.Hours -> context.getString(R.string.time_hours_ago, count.toInt())
}