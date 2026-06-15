package com.dinyairsadot.clearledger.core.util

import java.io.OutputStream

/**
 * Writes CSV text as UTF-8. Prepends a UTF-8 BOM (EF BB BF) only when [csv] contains
 * non-ASCII characters, so pure-ASCII exports avoid BOM display issues in mobile Sheets.
 */
object Utf8CsvWriter {

    private val UTF8_BOM_BYTES = byteArrayOf(
        0xEF.toByte(),
        0xBB.toByte(),
        0xBF.toByte()
    )

    fun writeUtf8CsvWithBom(outputStream: OutputStream, csv: String) {
        val contentBytes = csv.toByteArray(Charsets.UTF_8)
        if (containsNonAscii(csv)) {
            outputStream.write(UTF8_BOM_BYTES)
        }
        outputStream.write(contentBytes)
    }

    internal fun containsNonAscii(text: String): Boolean =
        text.any { it.code > 127 }
}
