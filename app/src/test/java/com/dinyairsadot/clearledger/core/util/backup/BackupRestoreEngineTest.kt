package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.backupZip
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.fileNames
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.invoice
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.payloadJson
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pdfBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.pngBytes
import com.dinyairsadot.clearledger.core.util.backup.BackupTestSupport.zipOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Staged-restore behaviour with an in-memory [BackupRestoreStore] and real files:
 * fresh install, restore over existing data, same-device restore, and every failure mode
 * (existing data and attachment files must survive any failed restore).
 */
class BackupRestoreEngineTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private class FakeStore(
        var categories: List<Category> = emptyList(),
        var invoices: List<Invoice> = emptyList()
    ) : BackupRestoreStore {
        var replaceCalls = 0
        var failOnReplace: Throwable? = null
        var throwAfterCommit: Throwable? = null
        var throwOnIsReferenced = false

        override suspend fun replaceAll(categories: List<Category>, invoices: List<Invoice>): Set<String> {
            replaceCalls++
            failOnReplace?.let { throw it }
            val replaced = this.invoices.mapNotNull { it.attachmentFileName }.toSet()
            this.categories = categories
            this.invoices = invoices
            throwAfterCommit?.let { throw it }
            return replaced
        }

        override suspend fun isAttachmentFileReferenced(fileName: String): Boolean {
            if (throwOnIsReferenced) throw IllegalStateException("store unavailable")
            return invoices.any { it.attachmentFileName == fileName }
        }
    }

    private fun engine(
        store: BackupRestoreStore,
        dir: File,
        limits: BackupZipImporter.Limits = BackupZipImporter.Limits()
    ) = BackupRestoreEngine(store, dir, limits = limits)

    private fun restore(engine: BackupRestoreEngine, zip: ByteArray) =
        runBlocking { engine.restore { zip.inputStream() } }

    private fun assertRestoreFails(engine: BackupRestoreEngine, zip: ByteArray) {
        try {
            restore(engine, zip)
            fail("Expected restore to fail")
        } catch (_: Exception) {
            // expected
        }
    }

    /** Two attachments (PDF + PNG) plus one invoice without, written under [dir]. */
    private fun sampleData(dir: File): BackupData {
        dir.mkdirs()
        File(dir, "aaa.pdf").writeBytes(pdfBytes)
        File(dir, "bbb.png").writeBytes(pngBytes)
        return BackupData(
            listOf(BackupTestSupport.category()),
            listOf(
                invoice(1, "aaa.pdf", "bill.pdf", "application/pdf"),
                invoice(2, "bbb.png", "photo.png", "image/png"),
                invoice(3)
            )
        )
    }

    // ---------------------------------------------------------------- success paths

    @Test
    fun freshInstall_restoresInvoicesWithAttachmentsUnderNewNames() {
        val data = sampleData(File(tmp.root, "deviceA"))
        val zip = backupZip(data, File(tmp.root, "deviceA"))
        val target = File(tmp.root, "deviceB/invoice_attachments") // does not exist yet
        val store = FakeStore()

        restore(engine(store, target), zip)

        val byId = store.invoices.associateBy { it.id }
        val pdfInvoice = byId.getValue(1)
        val pngInvoice = byId.getValue(2)
        assertNotEquals("aaa.pdf", pdfInvoice.attachmentFileName)
        assertTrue(pdfInvoice.attachmentFileName!!.endsWith(".pdf"))
        assertTrue(pngInvoice.attachmentFileName!!.endsWith(".png"))
        assertEquals("bill.pdf", pdfInvoice.attachmentDisplayName)
        assertEquals("application/pdf", pdfInvoice.attachmentMimeType)
        assertEquals("photo.png", pngInvoice.attachmentDisplayName)
        assertEquals("image/png", pngInvoice.attachmentMimeType)
        assertNull(pdfInvoice.attachmentUri)
        assertArrayEquals(pdfBytes, File(target, pdfInvoice.attachmentFileName!!).readBytes())
        assertArrayEquals(pngBytes, File(target, pngInvoice.attachmentFileName!!).readBytes())

        // The invoice without an attachment is restored exactly as it was.
        assertEquals(data.invoices[2], byId.getValue(3))
        assertEquals(data.categories, store.categories)
        assertEquals(2, fileNames(target).size)
    }

    @Test
    fun restoreOverExistingData_replacesRowsAndDeletesOnlyReplacedUnreferencedFiles() {
        val data = sampleData(File(tmp.root, "deviceA"))
        val zip = backupZip(data, File(tmp.root, "deviceA"))

        val target = tmp.newFolder("invoice_attachments")
        File(target, "old1.pdf").writeBytes(byteArrayOf(1))
        File(target, "old2.png").writeBytes(byteArrayOf(2))
        val orphanBytes = byteArrayOf(9, 9, 9)
        File(target, "unrelated-orphan.bin").writeBytes(orphanBytes)
        val store = FakeStore(
            categories = listOf(BackupTestSupport.category(7)),
            invoices = listOf(
                invoice(100, "old1.pdf", "o1.pdf", "application/pdf").copy(categoryId = 7),
                invoice(101, "old2.png", "o2.png", "image/png").copy(categoryId = 7)
            )
        )

        restore(engine(store, target), zip)

        assertEquals(setOf(1L, 2L, 3L), store.invoices.map { it.id }.toSet())
        val newNames = store.invoices.mapNotNull { it.attachmentFileName }.toSet()
        assertEquals(2, newNames.size)
        val remaining = fileNames(target)
        assertEquals(newNames + "unrelated-orphan.bin", remaining)
        assertFalse(File(target, "old1.pdf").exists())
        assertFalse(File(target, "old2.png").exists())
        assertArrayEquals(orphanBytes, File(target, "unrelated-orphan.bin").readBytes())
        newNames.forEach { assertTrue(File(target, it).length() > 0) }
    }

    @Test
    fun sameDeviceRestore_neverCollidesWithExistingFilesAndEndsWithNoDuplicates() {
        val dir = File(tmp.root, "invoice_attachments")
        val data = sampleData(dir)
        val zip = backupZip(data, dir)
        val store = FakeStore(categories = data.categories, invoices = data.invoices)

        restore(engine(store, dir), zip)

        val restoredNames = store.invoices.mapNotNull { it.attachmentFileName }.toSet()
        assertEquals(2, restoredNames.size)
        assertFalse(restoredNames.contains("aaa.pdf"))
        assertFalse(restoredNames.contains("bbb.png"))
        // Exactly the restored files remain: the originals were replaced, nothing orphaned.
        assertEquals(restoredNames, fileNames(dir))
        for (invoice in store.invoices.filter { it.attachmentFileName != null }) {
            val expected = if (invoice.id == 1L) pdfBytes else pngBytes
            assertArrayEquals(expected, File(dir, invoice.attachmentFileName!!).readBytes())
        }
    }

    @Test
    fun sharedAttachment_staysSharedAfterRestore() {
        val sourceDir = File(tmp.root, "deviceA").apply { mkdirs() }
        File(sourceDir, "shared.pdf").writeBytes(pdfBytes)
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(
                invoice(1, "shared.pdf", "a.pdf", "application/pdf"),
                invoice(2, "shared.pdf", "b.pdf", "application/pdf")
            )
        )
        val zip = backupZip(data, sourceDir)
        val target = File(tmp.root, "deviceB")
        val store = FakeStore()

        restore(engine(store, target), zip)

        val names = store.invoices.map { it.attachmentFileName }
        assertEquals(1, names.toSet().size)
        assertEquals(1, fileNames(target).size)
        assertEquals(listOf("a.pdf", "b.pdf"), store.invoices.map { it.attachmentDisplayName })
    }

    @Test
    fun version1BackupWithoutAttachments_stillRestores() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1), invoice(2))
        )
        val zip = zipOf("backup.json" to payloadJson(data, formatVersion = 1))
        val target = tmp.newFolder("invoice_attachments")
        val store = FakeStore()

        restore(engine(store, target), zip)

        assertEquals(data.invoices, store.invoices)
        assertTrue(fileNames(target).isEmpty())
    }

    @Test
    fun emptyBackup_restoresToEmptyStore() {
        val zip = zipOf("backup.json" to payloadJson(BackupData(emptyList(), emptyList())))
        val store = FakeStore(invoices = listOf(invoice(5)))

        restore(engine(store, tmp.newFolder("a")), zip)

        assertTrue(store.invoices.isEmpty())
        assertTrue(store.categories.isEmpty())
    }

    @Test
    fun untrustedDisplayName_cannotInfluenceRestoredFilePath() {
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "x.pdf", "../../../evil.pdf", "application/pdf"))
        )
        val json = payloadJson(data) { "attachments/x.pdf" }
        val zip = zipOf("backup.json" to json, "attachments/x.pdf" to pdfBytes)
        val target = File(tmp.root, "a/b/invoice_attachments")
        val store = FakeStore()

        restore(engine(store, target), zip)

        val name = store.invoices.single().attachmentFileName!!
        assertTrue(Regex("[0-9a-f-]{36}\\.pdf").matches(name))
        assertEquals(setOf(name), fileNames(target))
        assertFalse(File(tmp.root, "evil.pdf").exists())
        assertFalse(File(tmp.root, "a/evil.pdf").exists())
    }

    // ---------------------------------------------------------------- failure paths

    /** A store with existing data + a real old file, to prove failures leave them untouched. */
    private class Existing(val store: FakeStore, val dir: File, val before: Set<String>) {
        fun assertUntouched() {
            assertEquals(1, store.invoices.size)
            assertEquals("old.pdf", store.invoices.single().attachmentFileName)
            assertEquals(before, fileNames(dir))
            assertArrayEquals(byteArrayOf(42), File(dir, "old.pdf").readBytes())
        }
    }

    private fun existing(): Existing {
        val dir = tmp.newFolder("invoice_attachments")
        File(dir, "old.pdf").writeBytes(byteArrayOf(42))
        val store = FakeStore(
            categories = listOf(BackupTestSupport.category()),
            invoices = listOf(invoice(50, "old.pdf", "old.pdf", "application/pdf"))
        )
        return Existing(store, dir, fileNames(dir))
    }

    @Test
    fun missingReferencedEntry_failsAndLeavesEverythingUntouched() {
        val ex = existing()
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "a.pdf", "a.pdf", "application/pdf"), invoice(2, "b.png", "b.png", "image/png"))
        )
        // JSON references both files, but the ZIP only contains one of them.
        val json = payloadJson(data) { BackupFormat.attachmentEntryNameFor(it.attachmentFileName!!) }
        val zip = zipOf("backup.json" to json, "attachments/a.pdf" to pdfBytes)

        assertRestoreFails(engine(ex.store, ex.dir), zip)

        assertEquals(0, ex.store.replaceCalls)
        ex.assertUntouched()
    }

    @Test
    fun corruptedAttachmentData_failsCleansStagedFilesAndLeavesEverythingUntouched() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val data = sampleData(source)
        val good = backupZip(data, source)
        val corrupt = good.copyOf()
        val at = corrupt.size / 2 + 100
        for (i in at until at + 6) corrupt[i] = (corrupt[i].toInt() xor 0x77).toByte()

        assertRestoreFails(engine(ex.store, ex.dir), corrupt)

        assertEquals(0, ex.store.replaceCalls)
        ex.assertUntouched()
    }

    @Test
    fun truncatedZip_failsAndLeavesEverythingUntouched() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val good = backupZip(sampleData(source), source)

        assertRestoreFails(engine(ex.store, ex.dir), good.copyOf(good.size - 3_000))

        ex.assertUntouched()
    }

    @Test
    fun notAZip_failsAndLeavesEverythingUntouched() {
        val ex = existing()
        assertRestoreFails(engine(ex.store, ex.dir), "garbage".toByteArray())
        ex.assertUntouched()
    }

    @Test
    fun jsonReferencingTraversalEntry_isRejectedAndWritesNothingOutsideTheDirectory() {
        val ex = existing()
        val data = BackupData(
            listOf(BackupTestSupport.category()),
            listOf(invoice(1, "x.pdf", "x.pdf", "application/pdf"))
        )
        val json = payloadJson(data) { "attachments/../evil.pdf" }
        val zip = zipOf("backup.json" to json, "attachments/../evil.pdf" to pdfBytes)

        assertRestoreFails(engine(ex.store, ex.dir), zip)

        assertFalse(File(tmp.root, "evil.pdf").exists())
        assertFalse(File(ex.dir.parentFile, "evil.pdf").exists())
        ex.assertUntouched()
    }

    @Test
    fun unsafeZipEntryNotReferencedByJson_isRejected() {
        val ex = existing()
        val data = BackupData(listOf(BackupTestSupport.category()), listOf(invoice(1)))
        val zip = zipOf(
            "backup.json" to payloadJson(data),
            "../evil.pdf" to pdfBytes
        )

        assertRestoreFails(engine(ex.store, ex.dir), zip)

        assertFalse(File(tmp.root, "evil.pdf").exists())
        ex.assertUntouched()
    }

    @Test
    fun unreferencedAttachmentEntry_isRejected() {
        val ex = existing()
        val data = BackupData(listOf(BackupTestSupport.category()), listOf(invoice(1)))
        val zip = zipOf(
            "backup.json" to payloadJson(data),
            "attachments/stray.pdf" to pdfBytes
        )

        assertRestoreFails(engine(ex.store, ex.dir), zip)

        ex.assertUntouched()
    }

    @Test
    fun oversizedAttachment_failsWithLimitsAndCleansUp() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val zip = backupZip(sampleData(source), source)
        val tinyLimits = BackupZipImporter.Limits(maxAttachmentBytes = 1_000)

        assertRestoreFails(engine(ex.store, ex.dir, tinyLimits), zip)

        assertEquals(0, ex.store.replaceCalls)
        ex.assertUntouched()
    }

    @Test
    fun databaseFailure_removesStagedFilesAndKeepsOldData() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val zip = backupZip(sampleData(source), source)
        val failure = IllegalStateException("db boom")
        ex.store.failOnReplace = failure

        try {
            restore(engine(ex.store, ex.dir), zip)
            fail("Expected failure")
        } catch (e: IllegalStateException) {
            // (coroutines may re-create the exception across withContext, so compare message)
            assertEquals(failure.message, e.message)
        }

        assertEquals(1, ex.store.replaceCalls)
        ex.assertUntouched() // staged new files removed, old file and row intact
    }

    @Test
    fun failureAfterCommit_keepsStagedFilesBecauseTheDatabaseReferencesThem() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val zip = backupZip(sampleData(source), source)
        ex.store.throwAfterCommit = IOException("lost the result of a committed transaction")

        try {
            restore(engine(ex.store, ex.dir), zip)
            fail("Expected failure")
        } catch (_: IOException) {
            // expected
        }

        // Rows were committed, so the files they reference must not be deleted.
        val referenced = ex.store.invoices.mapNotNull { it.attachmentFileName }
        assertEquals(2, referenced.size)
        referenced.forEach { assertTrue("$it must still exist", File(ex.dir, it).isFile) }
    }

    @Test
    fun cleanupFailureAfterCommit_doesNotFailTheRestore() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val zip = backupZip(sampleData(source), source)
        ex.store.throwOnIsReferenced = true

        restore(engine(ex.store, ex.dir), zip) // must not throw

        assertEquals(setOf(1L, 2L, 3L), ex.store.invoices.map { it.id }.toSet())
        // Old file could not be cleaned up: harmless orphan, new files are in place.
        assertTrue(File(ex.dir, "old.pdf").exists())
        ex.store.invoices.mapNotNull { it.attachmentFileName }
            .forEach { assertTrue(File(ex.dir, it).isFile) }
    }

    @Test
    fun zipIsReadAgainDuringRestore_andRevalidatedEachTime() {
        val ex = existing()
        val source = File(tmp.root, "deviceA")
        val good = backupZip(sampleData(source), source)

        // Success path: the engine opens the ZIP once for JSON and once for attachments.
        var opens = 0
        runBlocking {
            engine(FakeStore(), tmp.newFolder("fresh")).restore {
                opens++
                good.inputStream()
            }
        }
        assertEquals(2, opens)

        // The file changes between the passes (valid JSON pass, then a ZIP lacking the files).
        val stripped = zipOf("backup.json" to BackupTestSupport.readEntry(good, "backup.json")!!)
        var call = 0
        val streams = listOf(good, stripped)
        try {
            runBlocking {
                engine(ex.store, ex.dir).restore {
                    val next: InputStream = streams[minOf(call, 1)].inputStream()
                    call++
                    next
                }
            }
            fail("Expected failure: the second pass is missing the attachments")
        } catch (_: IllegalArgumentException) {
            // expected
        }
        assertEquals(0, ex.store.replaceCalls)
        ex.assertUntouched()
    }

    @Test
    fun unreadableZip_failsWithoutTouchingAnything() {
        val ex = existing()
        try {
            runBlocking {
                engine(ex.store, ex.dir).restore { throw IOException("cannot open backup") }
            }
            fail("Expected failure")
        } catch (_: IOException) {
            // expected
        }
        ex.assertUntouched()
    }

    // ---------------------------------------------------------------- cleanup helper

    @Test
    fun cleanup_deletesUnreferencedKeepsReferencedAndIgnoresUnsafeNames() {
        val dir = tmp.newFolder("invoice_attachments")
        File(dir, "gone.pdf").writeBytes(byteArrayOf(1))
        File(dir, "kept.pdf").writeBytes(byteArrayOf(2))
        val outside = File(tmp.root, "outside.pdf").apply { writeBytes(byteArrayOf(3)) }

        runBlocking {
            AttachmentFileCleanup.deleteUnreferenced(
                dir,
                listOf("gone.pdf", "kept.pdf", "missing.pdf", "../outside.pdf", ""),
                { it == "kept.pdf" }
            )
        }

        assertFalse(File(dir, "gone.pdf").exists())
        assertTrue(File(dir, "kept.pdf").exists())
        assertTrue(outside.exists())
    }

    @Test
    fun cleanup_continuesPastAFailingLookup() {
        val dir = tmp.newFolder("invoice_attachments")
        File(dir, "a.pdf").writeBytes(byteArrayOf(1))
        File(dir, "b.pdf").writeBytes(byteArrayOf(2))

        runBlocking {
            AttachmentFileCleanup.deleteUnreferenced(dir, listOf("a.pdf", "b.pdf")) {
                if (it == "a.pdf") throw IllegalStateException("lookup failed")
                false
            }
        }

        assertTrue(File(dir, "a.pdf").exists())
        assertFalse(File(dir, "b.pdf").exists())
    }
}
