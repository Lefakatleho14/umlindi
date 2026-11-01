package com.project.umlindi.util

import com.project.umlindi.R
import java.util.concurrent.TimeUnit

/** How long ago a report was made, as data. The screen turns it into translated text. */

sealed interface TimeAgo {
    data object Unknown : TimeAgo
    data object JustNow : TimeAgo
    data class Minutes(val count: Long) : TimeAgo
    data class Hours(val count: Long) : TimeAgo
}

/**
 * Turns stored report values into display information. It returns string
 * resource ids instead of text, so the same code works in every language
 * and can be unit tested without Android.
 */
object ReportFormatter {

    fun categoryLabelRes(category: String): Int = when (category) {
        "break_in" -> R.string.category_break_in
        "suspicious_activity" -> R.string.category_suspicious
        "load_shedding_risk" -> R.string.category_load_shedding
        else -> R.string.category_other
    }

    fun severityLabelRes(category: String): Int = when (category) {
        "break_in" -> R.string.severity_high
        "suspicious_activity" -> R.string.severity_medium
        else -> R.string.severity_low
    }

    fun severityColorHex(category: String): String = when (category) {
        "break_in" -> "#D32F2F"
        "suspicious_activity" -> "#F57C00"
        else -> "#FBC02D"
    }

    fun severityBadgeColorHex(category: String): String = when (category) {
        "break_in" -> "#D32F2F"
        "suspicious_activity" -> "#EF6C00"
        else -> "#2E7D32"
    }

    /** nowMillis is a parameter so tests can pass a fixed time instead of the real clock. */

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