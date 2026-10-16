package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pdfBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pngBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.replaceBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.zipOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/** [BackupZipImporter.extractAttachments]: happy path and every strict-rejection rule. */
class BackupAttachmentImportTest {

    private val json = "{}".toByteArray()

    private class Capture {
        val outputs = LinkedHashMap<String, ByteArrayOutputStream>()
        fun open(name: String): OutputStream =
            ByteArrayOutputStream().also { outputs[name] = it }
    }

    private fun extract(
        zip: ByteArray,
        referenced: Set<String>,
        capture: Capture = Capture(),
        limits: BackupZipImporter.Limits = BackupZipImporter.Limits()
    ): Capture {
        BackupZipImporter.extractAttachments(zip.inputStream(), referenced, limits, capture::open)
        return capture
    }

    private fun assertRejected(
        zip: ByteArray,
        referenced: Set<String>,
        limits: BackupZipImporter.Limits = BackupZipImporter.Limits()
    ) {
        try {
            extract(zip, referenced, limits = limits)
            fail("Expected the ZIP to be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        } catch (_: java.io.IOException) {
            // ZipException (corrupt) is an IOException; also a rejection
        }
    }

    @Test
    fun extract_streamsReferencedEntriesByteForByte() {
        val zip = zipOf(
            "backup.json" to json,
            "attachments/a.pdf" to pdfBytes,
            "attachments/b.png" to pngBytes
        )

        val capture = extract(zip, setOf("attachments/a.pdf", "attachments/b.png"))

        assertArrayEquals(pdfBytes, capture.outputs.getValue("attachments/a.pdf").toByteArray())
        assertArrayEquals(pngBytes, capture.outputs.getValue("attachments/b.png").toByteArray())
    }

    @Test
    fun extract_version1StyleZipWithOnlyJson_succeedsWithNoReferences() {
        val capture = extract(zipOf("backup.json" to json), emptySet())
        assertTrue(capture.outputs.isEmpty())
    }

    @Test
    fun extract_attachmentBeforeJson_isAccepted() {
        val zip = zipOf("attachments/a.pdf" to pdfBytes, "backup.json" to json)
        val capture = extract(zip, setOf("attachments/a.pdf"))
        assertEquals(1, capture.outputs.size)
    }

    @Test
    fun extract_missingReferencedEntry_isRejected() {
        val zip = zipOf("backup.json" to json, "attachments/a.pdf" to pdfBytes)
        assertRejected(zip, setOf("attachments/a.pdf", "attachments/missing.pdf"))
    }

    @Test
    fun extract_unreferencedAttachmentEntry_isRejected() {
        val zip = zipOf(
            "backup.json" to json,
            "attachments/a.pdf" to pdfBytes,
            "attachments/extra.pdf" to pngBytes
        )
        assertRejected(zip, setOf("attachments/a.pdf"))
    }

    @Test
    fun extract_pathTraversalAndUnsafeEntryNames_areRejected() {
        val unsafe = listOf(
            "../evil.pdf",
            "attachments/../evil.pdf",
            "/etc/passwd",
            "attachments/sub/evil.pdf",
            "attachments\\evil.pdf",
            "evil.pdf",
            "other/evil.pdf",
            "attachments//evil.pdf",
            "attachments/..\\evil.pdf"
        )
        for (name in unsafe) {
            val zip = zipOf("backup.json" to json, name to pdfBytes)
            // Rejected even if the (malicious) JSON were to reference exactly that name.
            assertRejected(zip, emptySet())
            assertRejected(zip, setOf(name))
        }
    }

    @Test
    fun extract_directoryEntry_isRejected() {
        val zip = zipOf("backup.json" to json, "attachments/" to ByteArray(0))
        assertRejected(zip, emptySet())
    }

    @Test
    fun extract_duplicateEntryNames_areRejected() {
        val zip = replaceBytes(
            zipOf(
                "backup.json" to json,
                "attachments/a.pdf" to pdfBytes,
                "attachments/b.pdf" to pngBytes
            ),
            "attachments/b.pdf",
            "attachments/a.pdf"
        )
        assertRejected(zip, setOf("attachments/a.pdf"))
    }

    @Test
    fun extract_duplicateBackupJson_isRejected() {
        val zip = replaceBytes(
            zipOf("backup.json" to json, "backuq.json" to json),
            "backuq.json",
            "backup.json"
        )
        assertRejected(zip, emptySet())
    }

    @Test
    fun extract_tooManyEntries_isRejected() {
        val zip = zipOf(
            "backup.json" to json,
            "attachments/a.pdf" to pdfBytes,
            "attachments/b.pdf" to pdfBytes
        )
        assertRejected(
            zip,
            setOf("attachments/a.pdf", "attachments/b.pdf"),
            BackupZipImporter.Limits(maxEntries = 2)
        )
    }

    @Test
    fun extract_fileOverPerFileLimit_isRejected() {
        val zip = zipOf("backup.json" to json, "attachments/a.pdf" to ByteArray(11))
        assertRejected(
            zip,
            setOf("attachments/a.pdf"),
            BackupZipImporter.Limits(maxAttachmentBytes = 10)
        )
    }

    @Test
    fun extract_fileExactlyAtLimit_isAccepted() {
        val zip = zipOf("backup.json" to json, "attachments/a.pdf" to ByteArray(10))
        val capture = extract(
            zip,
            setOf("attachments/a.pdf"),
            limits = BackupZipImporter.Limits(maxAttachmentBytes = 10)
        )
        assertEquals(10, capture.outputs.getValue("attachments/a.pdf").size())
    }

    @Test
    fun extract_totalOverLimit_isRejectedWhileStreaming() {
        val zip = zipOf(
            "backup.json" to json,
            "attachments/a.pdf" to ByteArray(6),
            "attachments/b.pdf" to ByteArray(6)
        )
        assertRejected(
            zip,
            setOf("attachments/a.pdf", "attachments/b.pdf"),
            BackupZipImporter.Limits(maxAttachmentBytes = 100, maxTotalAttachmentBytes = 10)
        )
    }

    @Test
    fun extract_decompressionBomb_isStoppedByStreamingLimit() {
        // 5 MB of zeros compresses to a few KB, so the limit must be enforced on inflated bytes.
        val bomb = ByteArray(5 * 1024 * 1024)
        val zip = zipOf("backup.json" to json, "attachments/bomb.bin" to bomb)
        assertTrue("fixture should be highly compressed", zip.size < 100_000)
        val capture = Capture()
        try {
            extract(
                zip,
                setOf("attachments/bomb.bin"),
                capture,
                BackupZipImporter.Limits(maxAttachmentBytes = 1024 * 1024)
            )
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {
            // expected
        }
        assertTrue(capture.outputs.getValue("attachments/bomb.bin").size() <= 1024 * 1024)
    }

    @Test
    fun extract_oversizedJson_isRejected() {
        val zip = zipOf("backup.json" to ByteArray(100))
        assertRejected(zip, emptySet(), BackupZipImporter.Limits(maxJsonBytes = 50))
    }

    @Test
    fun readPayload_oversizedJson_isRejected() {
        val zip = zipOf("backup.json" to ByteArray(100))
        try {
            BackupZipImporter.readPayload(zip.inputStream(), maxJsonBytes = 50)
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun extract_corruptedEntryData_isRejected() {
        val content = ByteArray(4_000).also { java.util.Random(3).nextBytes(it) }
        val good = zipOf("backup.json" to json, "attachments/a.pdf" to content)
        val corrupt = good.copyOf()
        // Flip bytes inside the compressed attachment data (after the first local entry).
        val flipFrom = good.size / 2
        for (i in flipFrom until flipFrom + 4) corrupt[i] = (corrupt[i].toInt() xor 0x5A).toByte()

        assertRejected(corrupt, setOf("attachments/a.pdf"))
    }

    @Test
    fun extract_truncatedZip_isRejected() {
        val good = zipOf("backup.json" to json, "attachments/a.pdf" to pdfBytes)
        val truncated = good.copyOf(good.size / 2)
        assertRejected(truncated, setOf("attachments/a.pdf"))
    }

    @Test
    fun extract_notAZip_isRejected() {
        assertRejected("not a zip".toByteArray(), setOf("attachments/a.pdf"))
    }
}
