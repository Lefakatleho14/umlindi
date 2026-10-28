package com.project.umlindi.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertRulesTest {

    @Test
    fun `severe categories reach further than minor ones`() {
        assertEquals(10.0, AlertRules.effectiveRadiusKm("break_in", 5.0), 0.001)
        assertEquals(5.0, AlertRules.effectiveRadiusKm("suspicious_activity", 5.0), 0.001)
        assertEquals(2.5, AlertRules.effectiveRadiusKm("load_shedding_risk", 5.0), 0.001)
        assertEquals(2.5, AlertRules.effectiveRadiusKm("other", 5.0), 0.001)
    }

    @Test
    fun `break in at 8 km notifies a 5 km user`() {
        assertTrue(AlertRules.shouldNotify("break_in", 8.0, 5.0))
    }

    @Test
    fun `suspicious activity at 8 km does not notify a 5 km user`() {
        assertFalse(AlertRules.shouldNotify("suspicious_activity", 8.0, 5.0))
    }

    @Test
    fun `minor incident at 3 km does not notify a 5 km user`() {
        assertFalse(AlertRules.shouldNotify("load_shedding_risk", 3.0, 5.0))
    }

    @Test
    fun `unknown distance always notifies`() {
        assertTrue(AlertRules.shouldNotify("other", null, 5.0))
    }
}