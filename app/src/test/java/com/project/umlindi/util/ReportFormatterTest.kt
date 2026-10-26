package com.project.umlindi.util

import com.project.umlindi.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportFormatterTest {

    @Test
    fun `known categories map to their string resources`() {
        assertEquals(R.string.category_break_in, ReportFormatter.categoryLabelRes("break_in"))
        assertEquals(R.string.category_suspicious, ReportFormatter.categoryLabelRes("suspicious_activity"))
        assertEquals(R.string.category_load_shedding, ReportFormatter.categoryLabelRes("load_shedding_risk"))
    }

    @Test
    fun `unknown category falls back to other`() {
        assertEquals(R.string.category_other, ReportFormatter.categoryLabelRes("something_else"))
    }

    @Test
    fun `severity colors match expected category`() {
        assertEquals("#D32F2F", ReportFormatter.severityColorHex("break_in"))
        assertEquals("#F57C00", ReportFormatter.severityColorHex("suspicious_activity"))
        assertEquals("#FBC02D", ReportFormatter.severityColorHex("load_shedding_risk"))
    }

    @Test
    fun `time ago is classified relative to a fixed now`() {
        val now = 1_000_000_000L
        assertEquals(TimeAgo.JustNow, ReportFormatter.timeAgo(now - 30_000, now))
        assertEquals(TimeAgo.Minutes(5), ReportFormatter.timeAgo(now - 5 * 60_000, now))
        assertEquals(TimeAgo.Hours(2), ReportFormatter.timeAgo(now - 2 * 60 * 60_000, now))
    }

    @Test
    fun `null timestamp is unknown`() {
        assertEquals(TimeAgo.Unknown, ReportFormatter.timeAgo(null))
    }
}