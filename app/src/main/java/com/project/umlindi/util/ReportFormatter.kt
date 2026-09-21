package com.project.umlindi.util

import java.util.concurrent.TimeUnit

object ReportFormatter {

    fun formatCategory(raw: String): String = when (raw) {
        "break_in" -> "Break-in in progress"
        "suspicious_activity" -> "Suspicious activity"
        "load_shedding_risk" -> "Load-shedding risk"
        else -> "Other incident"
    }

    fun severityColorHex(category: String): String = when (category) {
        "break_in" -> "#D32F2F"
        "suspicious_activity" -> "#F57C00"
        else -> "#FBC02D"
    }

    fun formatTimeAgo(timestampMillis: Long?, nowMillis: Long = System.currentTimeMillis()): String {
        if (timestampMillis == null) return ""
        val diff = nowMillis - timestampMillis
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            else -> "${TimeUnit.MINUTES.toHours(minutes)} hr ago"
        }
    }
}