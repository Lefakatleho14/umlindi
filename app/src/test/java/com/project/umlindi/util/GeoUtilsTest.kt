package com.project.umlindi.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun `same point has zero distance`() {
        assertEquals(0.0, GeoUtils.distanceKm(-29.8587, 31.0218, -29.8587, 31.0218), 0.001)
    }

    @Test
    fun `one degree of latitude is about 111 km`() {
        assertEquals(111.19, GeoUtils.distanceKm(0.0, 0.0, 1.0, 0.0), 0.5)
    }

    @Test
    fun `durban to johannesburg is about 500 km`() {
        assertEquals(500.0, GeoUtils.distanceKm(-29.8587, 31.0218, -26.2041, 28.0473), 15.0)
    }
}