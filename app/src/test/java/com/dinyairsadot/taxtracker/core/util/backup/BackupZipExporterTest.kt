package com.dinyairsadot.taxtracker.core.util.backup

import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.DocumentType
import com.dinyairsadot.taxtracker.core.domain.Invoice
import com.dinyairsadot.taxtracker.core.domain.InvoiceCurrency
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream

class BackupZipExporterTest {

    private val gson = Gson()

    @Test
    fun writeZip_containsExactlyBackupJsonEntry() {
        val payload = writeAndParse(sampleBackupData())

        val entryNames = mutableListOf<String>()
        val output = ByteArrayOutputStream()
        BackupZipExporter.writeZip(output, sampleBackupData(), testMetadata())
        ZipInputStream(output.toByteArray().inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        assertEquals(listOf(BackupFormat.JSON_ENTRY_NAME), entryNames)
        assertNotNull(payload)
    }

    @Test
    fun writeZip_formatVersionIs1() {
        val payload = writeAndParse(sampleBackupData())
        assertEquals(1, payload.formatVersion)
    }

    @Test
    fun writeZip_metadataPresent() {
        val metadata = testMetadata()
        val payload = writeAndParse(sampleBackupData(), metadata)

        assertEquals(13, payload.metadata.dbSchemaVersion)
        assertEquals("tax-tracker-android", payload.metadata.producer)
        assertEquals("2026-06-11T12:00:00Z", payload.metadata.exportedAt)
    }

    @Test
    fun writeZip_allCategoryFieldsPresent() {
        val category = Category(
            id = 3L,
            name = "Electricity",
            colorHex = "#FF9800",
            description = "Bills",
            customFieldTitles = listOf("Meter", "Tariff"),
            supplierName = "Supplier Co",
            pinnedDefaults = mapOf("supplierName" to "Default Supplier"),
            seedKey = "electricity",
            userEdited = true,
            orderIndex = 1
        )
        val payload = writeAndParse(BackupData(categories = listOf(category), invoices = emptyList()))
        val dto = payload.categories.single()

        assertEquals(3L, dto.id)
        assertEquals("Electricity", dto.name)
        assertEquals("#FF9800", dto.colorHex)
        assertEquals("Bills", dto.description)
        assertEquals(listOf("Meter", "Tariff"), dto.customFieldTitles)
        assertEquals("Supplier Co", dto.supplierName)
        assertEquals(mapOf("supplierName" to "Default Supplier"), dto.pinnedDefaults)
        assertEquals("electricity", dto.seedKey)
        assertTrue(dto.userEdited)
        assertEquals(1, dto.orderIndex)
    }

    @Test
    fun writeZip_allInvoiceFieldsPresent() {
        val invoice = Invoice(
            id = 42L,
            categoryId = 3L,
            invoiceNumber = "INV-001",
            amount = 150.0,
            amountDue = 150.0,
            documentNumber = "DOC-001",
            paymentStatus = PaymentStatus.NOT_PAID,
            amountCurrency = InvoiceCurrency.ILS,
            vendorName = "Vendor",
            issueDate = LocalDate.of(2026, 1, 1),
            dueDate = LocalDate.of(2026, 2, 15),
            paymentDate = LocalDate.of(2026, 2, 10),
            servicePeriodStart = LocalDate.of(2026, 1, 1),
            servicePeriodEnd = LocalDate.of(2026, 1, 31),
            servicePeriodMode = ServicePeriodMode.MONTH,
            documentType = DocumentType.TAX_INVOICE,
            paymentMethod = "bank_transfer",
            numberOfPayments = "3",
            confirmationNumber = "CONF-1",
            consumptionValue = 100.5,
            consumptionUnit = "kWh",
            notes = "Note",
            customFieldValues = listOf("12345", "Peak"),
            pinnedSnapshot = mapOf("supplierName" to "Snap")
        )
        val payload = writeAndParse(BackupData(categories = emptyList(), invoices = listOf(invoice)))
        val dto = payload.invoices.single()

        assertEquals(42L, dto.id)
        assertEquals(3L, dto.categoryId)
        assertEquals("INV-001", dto.invoiceNumber)
        assertEquals(150.0, dto.amount, 0.001)
        assertEquals(150.0, dto.amountDue, 0.001)
        assertEquals("DOC-001", dto.documentNumber)
        assertEquals("NOT_PAID", dto.paymentStatus)
        assertEquals("ILS", dto.amountCurrency)
        assertEquals("Vendor", dto.vendorName)
        assertEquals("2026-01-01", dto.issueDate)
        assertEquals("2026-02-15", dto.dueDate)
        assertEquals("2026-02-10", dto.paymentDate)
        assertEquals("2026-01-01", dto.servicePeriodStart)
        assertEquals("2026-01-31", dto.servicePeriodEnd)
        assertEquals("MONTH", dto.servicePeriodMode)
        assertEquals("TAX_INVOICE", dto.documentType)
        assertEquals("bank_transfer", dto.paymentMethod)
        assertEquals("3", dto.numberOfPayments)
        assertEquals("CONF-1", dto.confirmationNumber)
        assertEquals(100.5, dto.consumptionValue!!, 0.001)
        assertEquals("kWh", dto.consumptionUnit)
        assertEquals("Note", dto.notes)
        assertEquals(listOf("12345", "Peak"), dto.customFieldValues)
        assertEquals(mapOf("supplierName" to "Snap"), dto.pinnedSnapshot)
    }

    @Test
    fun writeZip_enumValuesAreRawNames() {
        val invoice = Invoice(
            id = 1L,
            categoryId = 1L,
            invoiceNumber = "INV",
            amount = 10.0,
            paymentStatus = PaymentStatus.PAID,
            amountCurrency = InvoiceCurrency.USD,
            servicePeriodMode = ServicePeriodMode.DATE
        )
        val payload = writeAndParse(BackupData(categories = emptyList(), invoices = listOf(invoice)))
        val dto = payload.invoices.single()

        assertEquals("PAID", dto.paymentStatus)
        assertEquals("USD", dto.amountCurrency)
        assertEquals("DATE", dto.servicePeriodMode)
    }

    @Test
    fun writeZip_datesAreIso8601() {
        val invoice = Invoice(
            id = 1L,
            categoryId = 1L,
            invoiceNumber = "INV",
            amount = 10.0,
            paymentStatus = PaymentStatus.PAID,
            dueDate = LocalDate.of(2026, 3, 5)
        )
        val payload = writeAndParse(BackupData(categories = emptyList(), invoices = listOf(invoice)))
        assertEquals("2026-03-05", payload.invoices.single().dueDate)
    }

    @Test
    fun writeZip_nullDatesSerializedAsNull() {
        val invoice = Invoice(
            id = 1L,
            categoryId = 1L,
            invoiceNumber = "INV",
            amount = 10.0,
            paymentStatus = PaymentStatus.PAID,
            issueDate = null,
            dueDate = null,
            paymentDate = null,
            servicePeriodStart = null,
            servicePeriodEnd = null,
            documentType = null,
            vendorName = null,
            paymentMethod = null,
            numberOfPayments = null,
            confirmationNumber = null,
            consumptionValue = null,
            consumptionUnit = null,
            notes = null
        )
        val output = ByteArrayOutputStream()
        BackupZipExporter.writeZip(output, BackupData(emptyList(), listOf(invoice)), testMetadata())
        val json = readBackupJson(output.toByteArray())

        assertTrue(json.contains("\"issueDate\": null"))
        assertTrue(json.contains("\"dueDate\": null"))
        assertTrue(json.contains("\"documentType\": null"))

        val payload = gson.fromJson(json, BackupPayload::class.java)
        val dto = payload.invoices.single()
        assertNull(dto.issueDate)
        assertNull(dto.dueDate)
        assertNull(dto.documentType)
    }

    @Test
    fun writeZip_customFieldOrderPreserved() {
        val category = Category(
            id = 1L,
            name = "A",
            colorHex = "#000",
            customFieldTitles = listOf("First", "Second", "Third"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV",
            amount = 1.0,
            paymentStatus = PaymentStatus.PAID,
            customFieldValues = listOf("v1", "v2", "v3")
        )
        val payload = writeAndParse(BackupData(listOf(category), listOf(invoice)))

        assertEquals(listOf("First", "Second", "Third"), payload.categories.single().customFieldTitles)
        assertEquals(listOf("v1", "v2", "v3"), payload.invoices.single().customFieldValues)
    }

    @Test
    fun writeZip_categoryOrderPreserved() {
        val categories = listOf(
            Category(id = 1L, name = "First", colorHex = "#000", orderIndex = 0),
            Category(id = 2L, name = "Second", colorHex = "#111", orderIndex = 1)
        )
        val payload = writeAndParse(BackupData(categories, emptyList()))

        assertEquals(listOf(0, 1), payload.categories.map { it.orderIndex })
        assertEquals(listOf("First", "Second"), payload.categories.map { it.name })
    }

    @Test
    fun writeZip_hebrewContentRoundTrips() {
        val category = Category(
            id = 1L,
            name = "חשמל",
            colorHex = "#FF9800",
            orderIndex = 0
        )
        val payload = writeAndParse(BackupData(listOf(category), emptyList()))
        assertEquals("חשמל", payload.categories.single().name)
    }

    @Test
    fun writeZip_emptyDataIsValid() {
        val payload = writeAndParse(BackupData(emptyList(), emptyList()))

        assertTrue(payload.categories.isEmpty())
        assertTrue(payload.invoices.isEmpty())
        assertEquals(1, payload.formatVersion)
    }

    @Test
    fun writeZip_emptyCustomFields() {
        val category = Category(
            id = 1L,
            name = "A",
            colorHex = "#000",
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            orderIndex = 0
        )
        val payload = writeAndParse(BackupData(listOf(category), emptyList()))
        val dto = payload.categories.single()

        assertTrue(dto.customFieldTitles.isEmpty())
        assertTrue(dto.pinnedDefaults.isEmpty())
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
            amountCurrency = InvoiceCurrency.ILS,
            servicePeriodMode = ServicePeriodMode.MONTH
        )
        return BackupData(listOf(category), listOf(invoice))
    }

    private fun testMetadata(): BackupMetadata = BackupMetadata(
        exportedAt = "2026-06-11T12:00:00Z",
        dbSchemaVersion = BackupFormat.DB_SCHEMA_VERSION,
        producer = BackupFormat.PRODUCER
    )

    private fun writeAndParse(
        data: BackupData,
        metadata: BackupMetadata = testMetadata()
    ): BackupPayload {
        val output = ByteArrayOutputStream()
        BackupZipExporter.writeZip(output, data, metadata)
        val json = readBackupJson(output.toByteArray())
        return gson.fromJson(json, BackupPayload::class.java)
    }

    private fun readBackupJson(zipBytes: ByteArray): String {
        return ZipInputStream(zipBytes.inputStream()).use { zip ->
            val entry = zip.nextEntry
            check(entry?.name == BackupFormat.JSON_ENTRY_NAME)
            zip.readBytes().toString(Charsets.UTF_8)
        }
    }
}
