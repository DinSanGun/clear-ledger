package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.entryNames
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.invoice
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pdfBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pngBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.readEntry
import com.google.gson.Gson
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class BackupAttachmentExportTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val gson = Gson()

    private fun sourceDir(): File = tmp.newFolder("source")

    private fun payloadOf(zip: ByteArray): BackupPayload =
        gson.fromJson(
            readEntry(zip, BackupFormat.JSON_ENTRY_NAME)!!.toString(Charsets.UTF_8),
            BackupPayload::class.java
        )

    @Test
    fun export_imageAndPdf_areStoredAsEntriesWithMetadata() {
        val dir = sourceDir()
        File(dir, "aaa.pdf").writeBytes(pdfBytes)
        File(dir, "bbb.png").writeBytes(pngBytes)
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(
                invoice(1, "aaa.pdf", "bill.pdf", "application/pdf"),
                invoice(2, "bbb.png", "photo.png", "image/png")
            )
        )

        val zip = BackupTestSupport.backupZip(data, dir)

        assertEquals(
            listOf("backup.json", "attachments/aaa.pdf", "attachments/bbb.png"),
            entryNames(zip)
        )
        assertArrayEquals(pdfBytes, readEntry(zip, "attachments/aaa.pdf"))
        assertArrayEquals(pngBytes, readEntry(zip, "attachments/bbb.png"))

        val invoices = payloadOf(zip).invoices.associateBy { it.id }
        assertEquals(
            BackupAttachmentDto("attachments/aaa.pdf", "bill.pdf", "application/pdf"),
            invoices.getValue(1).attachment
        )
        assertEquals(
            BackupAttachmentDto("attachments/bbb.png", "photo.png", "image/png"),
            invoices.getValue(2).attachment
        )
    }

    @Test
    fun export_invoiceWithoutAttachment_hasNullAttachmentAndNoExtraEntries() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1))
        )

        val zip = BackupTestSupport.backupZip(data, sourceDir())

        assertEquals(listOf("backup.json"), entryNames(zip))
        assertNull(payloadOf(zip).invoices.single().attachment)
    }

    @Test
    fun export_sharedFile_isWrittenOnceAndReferencedByBothInvoices() {
        val dir = sourceDir()
        File(dir, "shared.pdf").writeBytes(pdfBytes)
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(
                invoice(1, "shared.pdf", "a.pdf", "application/pdf"),
                invoice(2, "shared.pdf", "b.pdf", "application/pdf")
            )
        )

        val zip = BackupTestSupport.backupZip(data, dir)

        assertEquals(listOf("backup.json", "attachments/shared.pdf"), entryNames(zip))
        val invoices = payloadOf(zip).invoices
        assertEquals(setOf("attachments/shared.pdf"), invoices.map { it.attachment!!.entryName }.toSet())
        // Per-invoice metadata is preserved even though the bytes are shared.
        assertEquals(listOf("a.pdf", "b.pdf"), invoices.map { it.attachment!!.displayName })
    }

    @Test
    fun export_legacyUriOnlyInvoice_isSkippedWithoutAttachment() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, legacyUri = "content://docs/legacy/1"))
        )

        val zip = BackupTestSupport.backupZip(data, sourceDir())

        assertEquals(listOf("backup.json"), entryNames(zip))
        val dto = payloadOf(zip).invoices.single()
        assertNull(dto.attachment)
        assertFalse(String(readEntry(zip, "backup.json")!!).contains("content://"))
    }

    @Test
    fun export_missingAttachmentFile_failsAndWritesNothing() {
        val dir = sourceDir()
        File(dir, "present.pdf").writeBytes(pdfBytes)
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(
                invoice(1, "present.pdf", "a.pdf", "application/pdf"),
                invoice(2, "gone.pdf", "b.pdf", "application/pdf")
            )
        )
        val output = ByteArrayOutputStream()

        try {
            BackupZipExporter.writeZip(
                output, data, BackupTestSupport.metadata(), DirectoryBackupAttachmentSource(dir)
            )
            fail("Expected backup to fail for a missing attachment")
        } catch (_: IOException) {
            // expected
        }
        assertEquals("No partial ZIP may be produced", 0, output.size())
    }

    @Test
    fun export_attachmentThatCannotBeOpenedMidWrite_fails() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "flaky.pdf", "a.pdf", "application/pdf"))
        )
        var opens = 0
        val flakySource = BackupAttachmentSource {
            opens++
            if (opens == 1) pdfBytes.inputStream() else null
        }

        try {
            BackupZipExporter.writeZip(
                ByteArrayOutputStream(), data, BackupTestSupport.metadata(), flakySource
            )
            fail("Expected failure when the file disappears between check and copy")
        } catch (_: IOException) {
            // expected
        }
    }

    @Test
    fun export_noSourceProvided_failsWhenInvoiceHasManagedAttachment() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "x.pdf", "x.pdf", "application/pdf"))
        )
        try {
            BackupZipExporter.writeZip(ByteArrayOutputStream(), data, BackupTestSupport.metadata())
            fail("Expected failure")
        } catch (_: IOException) {
            // expected
        }
    }

    @Test
    fun export_unsafeManagedFileNameInDatabase_fails() {
        val dir = sourceDir()
        File(tmp.root, "secret.pdf").writeBytes(pdfBytes)
        for (name in listOf("../secret.pdf", "sub/x.pdf", "..", "a\\b.pdf")) {
            val data = BackupData(
                listOf(BackupTestSupport.category()),
                listOf(invoice(1, name, "x.pdf", "application/pdf"))
            )
            val output = ByteArrayOutputStream()
            try {
                BackupZipExporter.writeZip(
                    output, data, BackupTestSupport.metadata(), DirectoryBackupAttachmentSource(dir)
                )
                fail("Expected failure for unsafe name '$name'")
            } catch (_: IOException) {
                // expected
            }
            assertEquals(0, output.size())
        }
    }

    @Test
    fun export_largeFile_isStreamedByteForByte() {
        val dir = sourceDir()
        val big = ByteArray(3 * 1024 * 1024 + 17) { (it % 253).toByte() }
        File(dir, "big.png").writeBytes(big)
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "big.png", "big.png", "image/png"))
        )

        val zip = BackupTestSupport.backupZip(data, dir)

        assertArrayEquals(big, readEntry(zip, "attachments/big.png"))
    }

    // --- DirectoryBackupAttachmentSource / BackupFormat helpers ---

    @Test
    fun directorySource_opensExistingFile_andRejectsMissingDirectoryAndUnsafeNames() {
        val dir = sourceDir()
        File(dir, "ok.pdf").writeBytes(pdfBytes)
        File(dir, "folder.pdf").mkdir()
        File(tmp.root, "outside.pdf").writeBytes(pdfBytes)
        val source = DirectoryBackupAttachmentSource(dir)

        assertNotNull(source.open("ok.pdf")?.also { it.close() })
        assertNull(source.open("missing.pdf"))
        assertNull(source.open("folder.pdf"))
        assertNull(source.open("../outside.pdf"))
        assertNull(source.open(""))
    }

    @Test
    fun format_safeNames() {
        assertTrue(BackupFormat.isSafeAttachmentFileName("3f2a-uuid.pdf"))
        assertTrue(BackupFormat.isSafeAttachmentFileName("noextension"))
        assertTrue(BackupFormat.isSafeAttachmentFileName("חשבונית.pdf"))
        assertFalse(BackupFormat.isSafeAttachmentFileName(null))
        assertFalse(BackupFormat.isSafeAttachmentFileName(""))
        assertFalse(BackupFormat.isSafeAttachmentFileName("."))
        assertFalse(BackupFormat.isSafeAttachmentFileName(".."))
        assertFalse(BackupFormat.isSafeAttachmentFileName("a/b"))
        assertFalse(BackupFormat.isSafeAttachmentFileName("a\\b"))
        assertFalse(BackupFormat.isSafeAttachmentFileName("a..b"))
        assertFalse(BackupFormat.isSafeAttachmentFileName("a\nb"))
        assertTrue(BackupFormat.isSafeAttachmentEntryName("attachments/x.pdf"))
        assertFalse(BackupFormat.isSafeAttachmentEntryName("x.pdf"))
        assertFalse(BackupFormat.isSafeAttachmentEntryName("attachments/"))
        assertFalse(BackupFormat.isSafeAttachmentEntryName("attachments/a/b"))
    }
}
