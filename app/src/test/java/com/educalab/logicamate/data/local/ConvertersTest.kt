package com.educalab.logicamate.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `toCsv then fromCsv round-trips a list of strings`() {
        val list = listOf("A", "B", "C")
        assertEquals(list, converters.fromCsv(converters.toCsv(list)))
    }

    @Test
    fun `empty list encodes to an empty string`() {
        assertEquals("", converters.toCsv(emptyList()))
    }

    @Test
    fun `fromCsv on an empty string returns an empty list, not a list with one blank item`() {
        assertTrue(converters.fromCsv("").isEmpty())
    }

    @Test
    fun `fromCsv handles a null input gracefully`() {
        assertTrue(converters.fromCsv(null).isEmpty())
    }

    @Test
    fun `toCsv handles a null input gracefully`() {
        assertEquals("", converters.toCsv(null))
    }
}
