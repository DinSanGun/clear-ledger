package com.dinyairsadot.taxtracker.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class Utf8CsvWriterTest {

    @Test
    fun writeUtf8CsvWithBom_asciiOnly_doesNotWriteBom() {
        val output = ByteArrayOutputStream()
        val csv = "Category Name,Invoice Number,ILS,USD"
        Utf8CsvWriter.writeUtf8CsvWithBom(output, csv)

        val bytes = output.toByteArray()
        assertEquals('C'.code.toByte(), bytes[0])
        assertEquals(csv, bytes.toString(Charsets.UTF_8))
        assertFalse(Utf8CsvWriter.containsNonAscii(csv))
    }

    @Test
    fun writeUtf8CsvWithBom_nonAscii_writesBomBytesThenUtf8Content() {
        val output = ByteArrayOutputStream()
        val csv = "שם קטגוריה,הערות"
        Utf8CsvWriter.writeUtf8CsvWithBom(output, csv)

        val bytes = output.toByteArray()
        assertEquals(0xEF.toByte(), bytes[0])
        assertEquals(0xBB.toByte(), bytes[1])
        assertEquals(0xBF.toByte(), bytes[2])
        assertTrue(Utf8CsvWriter.containsNonAscii(csv))

        val contentWithoutBom = bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
        assertEquals(csv, contentWithoutBom)
    }

    @Test
    fun containsNonAscii_detectsHebrewAndAscii() {
        assertFalse(Utf8CsvWriter.containsNonAscii("Category Name,ILS"))
        assertTrue(Utf8CsvWriter.containsNonAscii("שם קטגוריה"))
        assertTrue(Utf8CsvWriter.containsNonAscii("Notes,שולם"))
    }
}
