package com.project.umlindi.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TrustRulesTest {

    @Test
    fun `first confirm adds one point`() {
        assertEquals(1, TrustRules.delta(null, TrustRules.CONFIRM))
    }

    @Test
    fun `first dispute removes one point`() {
        assertEquals(-1, TrustRules.delta(null, TrustRules.DISPUTE))
    }

    @Test
    fun `switching confirm to dispute swings by two`() {
        assertEquals(-2, TrustRules.delta(TrustRules.CONFIRM, TrustRules.DISPUTE))
    }

    @Test
    fun `switching dispute to confirm swings by two`() {
        assertEquals(2, TrustRules.delta(TrustRules.DISPUTE, TrustRules.CONFIRM))
    }

    @Test
    fun `repeating the same vote changes nothing`() {
        assertEquals(0, TrustRules.delta(TrustRules.CONFIRM, TrustRules.CONFIRM))
        assertEquals(0, TrustRules.delta(TrustRules.DISPUTE, TrustRules.DISPUTE))
    }
}