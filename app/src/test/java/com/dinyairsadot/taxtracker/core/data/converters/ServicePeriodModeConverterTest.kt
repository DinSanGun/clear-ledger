package com.dinyairsadot.taxtracker.core.data.converters

import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServicePeriodModeConverterTest {

    private val converter = ServicePeriodModeConverter()

    // ── fromServicePeriodMode ────────────────────────────────────────────────

    @Test
    fun `fromServicePeriodMode MONTH returns 'MONTH'`() {
        assertEquals("MONTH", converter.fromServicePeriodMode(ServicePeriodMode.MONTH))
    }

    @Test
    fun `fromServicePeriodMode DATE returns 'DATE'`() {
        assertEquals("DATE", converter.fromServicePeriodMode(ServicePeriodMode.DATE))
    }

    @Test
    fun `fromServicePeriodMode null returns null`() {
        assertNull(converter.fromServicePeriodMode(null))
    }

    // ── toServicePeriodMode ──────────────────────────────────────────────────

    @Test
    fun `toServicePeriodMode 'MONTH' returns MONTH`() {
        assertEquals(ServicePeriodMode.MONTH, converter.toServicePeriodMode("MONTH"))
    }

    @Test
    fun `toServicePeriodMode 'DATE' returns DATE`() {
        assertEquals(ServicePeriodMode.DATE, converter.toServicePeriodMode("DATE"))
    }

    @Test
    fun `toServicePeriodMode null returns null`() {
        assertNull(converter.toServicePeriodMode(null))
    }

    @Test
    fun `toServicePeriodMode unknown string falls back to MONTH`() {
        // Unknown values from a future build or corrupt data must not crash.
        assertEquals(ServicePeriodMode.MONTH, converter.toServicePeriodMode("WEEK"))
    }

    // ── round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `round-trip MONTH`() {
        val stored = converter.fromServicePeriodMode(ServicePeriodMode.MONTH)
        assertEquals(ServicePeriodMode.MONTH, converter.toServicePeriodMode(stored))
    }

    @Test
    fun `round-trip DATE`() {
        val stored = converter.fromServicePeriodMode(ServicePeriodMode.DATE)
        assertEquals(ServicePeriodMode.DATE, converter.toServicePeriodMode(stored))
    }
}
