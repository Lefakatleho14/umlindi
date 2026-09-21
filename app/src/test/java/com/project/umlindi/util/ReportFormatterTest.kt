package com.project.umlindi.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportFormatterTest {

    @Test
    fun `known categories map to readable labels`() {
        assertEquals("Break-in in progress", ReportFormatter.formatCategory("break_in"))
        assertEquals("Suspicious activity", ReportFormatter.formatCategory("suspicious_activity"))
        assertEquals("Load-shedding risk", ReportFormatter.formatCategory("load_shedding_risk"))
    }

    @Test
    fun `unknown category falls back to Other incident`() {
        assertEquals("Other incident", ReportFormatter.formatCategory("something_else"))
    }

    @Test
    fun `severity colors match expected category`() {
        assertEquals("#D32F2F", ReportFormatter.severityColorHex("break_in"))
        assertEquals("#F57C00", ReportFormatter.severityColorHex("suspicious_activity"))
        assertEquals("#FBC02D", ReportFormatter.severityColorHex("load_shedding_risk"))
    }

    @Test
    fun `time ago formats correctly relative to a fixed now`() {
        val now = 1_000_000_000L
        assertEquals("Just now", ReportFormatter.formatTimeAgo(now - 30_000, now)) // 30 sec ago
        assertEquals("5 min ago", ReportFormatter.formatTimeAgo(now - 5 * 60_000, now))
        assertEquals("2 hr ago", ReportFormatter.formatTimeAgo(now - 2 * 60 * 60_000, now))
    }

    @Test
    fun `null timestamp returns empty string`() {
        assertEquals("", ReportFormatter.formatTimeAgo(null))
    }
}