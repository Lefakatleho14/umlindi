package com.project.umlindi.util

import com.project.umlindi.R
import java.util.concurrent.TimeUnit

sealed interface TimeAgo {
    data object Unknown : TimeAgo
    data object JustNow : TimeAgo
    data class Minutes(val count: Long) : TimeAgo
    data class Hours(val count: Long) : TimeAgo
}

object ReportFormatter {

    fun categoryLabelRes(category: String): Int = when (category) {
        "break_in" -> R.string.category_break_in
        "suspicious_activity" -> R.string.category_suspicious
        "load_shedding_risk" -> R.string.category_load_shedding
        else -> R.string.category_other
    }

    fun severityColorHex(category: String): String = when (category) {
        "break_in" -> "#D32F2F"
        "suspicious_activity" -> "#F57C00"
        else -> "#FBC02D"
    }

    fun timeAgo(timestampMillis: Long?, nowMillis: Long = System.currentTimeMillis()): TimeAgo {
        if (timestampMillis == null) return TimeAgo.Unknown
        val minutes = TimeUnit.MILLISECONDS.toMinutes(nowMillis - timestampMillis)
        return when {
            minutes < 1 -> TimeAgo.JustNow
            minutes < 60 -> TimeAgo.Minutes(minutes)
            else -> TimeAgo.Hours(TimeUnit.MINUTES.toHours(minutes))
        }
    }
}