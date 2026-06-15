package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupZipImporterTest {

    @Test
    fun readPayload_validZip_returnsParsedPayload() {
        val data = sampleBackupData()
        val zipBytes = writeZipBytes(data)

        val payload = BackupZipImporter.readPayload(ByteArrayInputStream(zipBytes))

        assertEquals(1, payload.formatVersion)
        assertNotNull(payload.metadata)
        assertEquals(1, payload.categories.size)
        assertEquals(1, payload.invoices.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun readPayload_wrongEntryName_throws() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("data.json"))
            zip.write("""{"formatVersion":1}""".toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }

        BackupZipImporter.readPayload(ByteArrayInputStream(output.toByteArray()))
    }

    @Test(expected = Exception::class)
    fun readPayload_notZip_throws() {
        BackupZipImporter.readPayload(
            ByteArrayInputStream("not a zip file".toByteArray(StandardCharsets.UTF_8))
        )
    }

    @Test(expected = Exception::class)
    fun readPayload_malformedJson_throws() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFormat.JSON_ENTRY_NAME))
            zip.write("{ invalid json".toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }

        BackupZipImporter.readPayload(ByteArrayInputStream(output.toByteArray()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun readPayload_emptyJson_throws() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFormat.JSON_ENTRY_NAME))
            zip.closeEntry()
        }

        BackupZipImporter.readPayload(ByteArrayInputStream(output.toByteArray()))
    }

    @Test
    fun readPayload_fullRoundTrip_preservesDomainData() {
        val category = Category(
            id = 3L,
            name = "Electricity",
            colorHex = "#FF9800",
            description = "Bills",
            customFieldTitles = listOf("T1", "T2", "T3"),
            supplierName = "Supplier Co",
            pinnedDefaults = mapOf("supplierName" to "Default Supplier"),
            seedKey = "electricity",
            userEdited = true,
            orderIndex = 1
        )
        val invoice = Invoice(
            id = 42L,
            categoryId = 3L,
            invoiceNumber = "INV-001",
            amount = 150.0,
            amountDue = 150.0,
            documentNumber = "DOC-001",
            paymentStatus = PaymentStatus.NOT_PAID,
            amountCurrency = InvoiceCurrency.USD,
            servicePeriodStart = LocalDate.of(2026, 1, 1),
            servicePeriodEnd = LocalDate.of(2026, 1, 31),
            servicePeriodMode = ServicePeriodMode.DATE,
            dueDate = LocalDate.of(2026, 2, 15),
            paymentDate = LocalDate.of(2026, 2, 10),
            customFieldValues = listOf("a", "", "c")
        )
        val data = BackupData(listOf(category), listOf(invoice))
        val zipBytes = writeZipBytes(data)

        val payload = BackupZipImporter.readPayload(ByteArrayInputStream(zipBytes))
        val validation = BackupValidator.validate(payload)
        assertTrue(validation is BackupValidationResult.Valid)

        val restoredCategory = BackupMapper.fromCategoryDto(payload.categories.single())
        val restoredInvoice = BackupMapper.fromInvoiceDto(payload.invoices.single())

        assertEquals(category, restoredCategory)
        assertEquals(invoice, restoredInvoice)
    }

    @Test(expected = IllegalArgumentException::class)
    fun readPayload_exportStyleZipWithoutBackupJson_throws() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("categories.csv"))
            zip.write("Category Name,Description,Order".toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }

        BackupZipImporter.readPayload(ByteArrayInputStream(output.toByteArray()))
    }

    private fun sampleBackupData(): BackupData {
        val category = Category(
            id = 1L,
            name = "Utilities",
            colorHex = "#FF9800",
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 100.0,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH
        )
        return BackupData(listOf(category), listOf(invoice))
    }

    private fun writeZipBytes(data: BackupData): ByteArray {
        val output = ByteArrayOutputStream()
        BackupZipExporter.writeZip(output, data, testMetadata())
        return output.toByteArray()
    }

    private fun testMetadata(): BackupMetadata = BackupMetadata(
        exportedAt = "2026-06-11T12:00:00Z",
        dbSchemaVersion = BackupFormat.DB_SCHEMA_VERSION,
        producer = BackupFormat.PRODUCER
    )
}
