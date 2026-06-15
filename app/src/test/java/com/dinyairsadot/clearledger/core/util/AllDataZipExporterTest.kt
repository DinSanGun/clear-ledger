package com.dinyairsadot.clearledger.core.util

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream

class AllDataZipExporterTest {

    @Test
    fun sanitizeCategoryName_replacesUnsafePathCharacters() {
        assertEquals("Phone _ Internet", AllDataZipExporter.sanitizeCategoryName("Phone / Internet"))
    }

    @Test
    fun sanitizeCategoryName_preservesHebrew() {
        assertEquals("שלום עולם", AllDataZipExporter.sanitizeCategoryName("שלום עולם"))
    }

    @Test
    fun sanitizeCategoryName_preservesReadableEnglish() {
        assertEquals("Electricity", AllDataZipExporter.sanitizeCategoryName("Electricity"))
    }

    @Test
    fun writeZip_containsCategoriesAndInvoiceEntries() {
        val category = Category(
            id = 1L,
            name = "Utilities",
            colorHex = "#FF9800",
            description = "Bills",
            customFieldTitles = listOf("Meter"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 100.0,
            amountCurrency = InvoiceCurrency.ILS,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStart = LocalDate.of(2026, 1, 1),
            servicePeriodEnd = LocalDate.of(2026, 1, 31),
            dueDate = LocalDate.of(2026, 2, 1),
            customFieldValues = listOf("123")
        )
        val data = AllExportData(
            categories = listOf(category),
            invoicesByCategory = mapOf(1L to listOf(invoice))
        )
        val output = ByteArrayOutputStream()
        AllDataZipExporter.writeZip(
            outputStream = output,
            data = data,
            invoiceLabels = testInvoiceLabels(),
            categoryLabels = testCategoryLabels()
        )

        val entryNames = mutableListOf<String>()
        ZipInputStream(output.toByteArray().inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        assertTrue(entryNames.contains("categories.csv"))
        assertTrue(entryNames.contains("invoices/Utilities_1.csv"))
    }

    @Test
    fun writeZip_skipsEmptyInvoiceCsv() {
        val withInvoices = Category(id = 1L, name = "A", colorHex = "#000", orderIndex = 0)
        val empty = Category(id = 2L, name = "B", colorHex = "#111", orderIndex = 1)
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 50.0,
            amountCurrency = InvoiceCurrency.ILS,
            paymentStatus = PaymentStatus.PAID,
            servicePeriodMode = ServicePeriodMode.MONTH
        )
        val data = AllExportData(
            categories = listOf(withInvoices, empty),
            invoicesByCategory = mapOf(1L to listOf(invoice), 2L to emptyList())
        )
        val output = ByteArrayOutputStream()
        AllDataZipExporter.writeZip(
            outputStream = output,
            data = data,
            invoiceLabels = testInvoiceLabels(),
            categoryLabels = testCategoryLabels()
        )

        val entryNames = mutableListOf<String>()
        ZipInputStream(output.toByteArray().inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        assertTrue(entryNames.contains("categories.csv"))
        assertTrue(entryNames.contains("invoices/A_1.csv"))
        assertFalse(entryNames.any { it.startsWith("invoices/B_") })
    }

    @Test
    fun buildCategoriesCsv_excludesColorColumn() {
        val categories = listOf(
            Category(
                id = 1L,
                name = "A",
                colorHex = "#FF0000",
                description = "Desc",
                customFieldTitles = listOf("Field1"),
                orderIndex = 0
            )
        )
        val csv = AllDataZipExporter.buildCategoriesCsv(categories, testCategoryLabels())
        val header = csv.lines().first()
        assertTrue(header.contains("Category Name"))
        assertTrue(header.contains("Description"))
        assertTrue(header.contains("Order"))
        assertFalse(header.contains("Color"))
    }

    @Test
    fun buildCategoriesCsv_includesCustomFieldColumns() {
        val categories = listOf(
            Category(
                id = 1L,
                name = "A",
                colorHex = "#000",
                customFieldTitles = listOf("Field1"),
                orderIndex = 0
            ),
            Category(
                id = 2L,
                name = "B",
                colorHex = "#111",
                customFieldTitles = listOf("X", "Y"),
                orderIndex = 1
            )
        )
        val csv = AllDataZipExporter.buildCategoriesCsv(categories, testCategoryLabels())
        val lines = csv.lines()
        assertEquals(3, lines.size)
        assertTrue(lines[0].contains("Custom Field 1 Title"))
        assertTrue(lines[0].contains("Custom Field 2 Title"))
    }

    @Test
    fun writeZip_hebrewCategoryName_usesHebrewInFilename() {
        val category = Category(
            id = 1L,
            name = "ארנונה",
            colorHex = "#FF9800",
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 100.0,
            amountCurrency = InvoiceCurrency.ILS,
            paymentStatus = PaymentStatus.PAID,
            servicePeriodMode = ServicePeriodMode.MONTH
        )
        val zipBytes = writeZipBytes(
            AllExportData(
                categories = listOf(category),
                invoicesByCategory = mapOf(1L to listOf(invoice))
            )
        )

        val entryNames = listZipEntryNames(zipBytes)
        assertTrue(entryNames.contains("invoices/ארנונה_1.csv"))
    }

    @Test
    fun writeZip_invoiceCsv_customFieldTitlesAndValuesAligned() {
        val category = Category(
            id = 1L,
            name = "Utilities",
            colorHex = "#FF9800",
            customFieldTitles = listOf("Meter"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 100.0,
            amountCurrency = InvoiceCurrency.ILS,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            customFieldValues = listOf("123")
        )
        val zipBytes = writeZipBytes(
            AllExportData(
                categories = listOf(category),
                invoicesByCategory = mapOf(1L to listOf(invoice))
            )
        )

        val csv = readZipEntryText(zipBytes, "invoices/Utilities_1.csv")
        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].endsWith("Meter"))
        assertTrue(lines[1].endsWith("123"))
    }

    @Test
    fun writeZip_hebrewInvoiceCsv_writesBomAndPreservesHebrew() {
        val category = Category(
            id = 1L,
            name = "ארנונה",
            colorHex = "#FF9800",
            customFieldTitles = listOf("מונה"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 100.0,
            amountCurrency = InvoiceCurrency.ILS,
            paymentStatus = PaymentStatus.PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            customFieldValues = listOf("ערך")
        )
        val zipBytes = writeZipBytes(
            AllExportData(
                categories = listOf(category),
                invoicesByCategory = mapOf(1L to listOf(invoice))
            )
        )

        val entryBytes = readZipEntryBytes(zipBytes, "invoices/ארנונה_1.csv")
        assertEquals(0xEF.toByte(), entryBytes[0])
        assertEquals(0xBB.toByte(), entryBytes[1])
        assertEquals(0xBF.toByte(), entryBytes[2])

        val csv = entryBytes.copyOfRange(3, entryBytes.size).toString(Charsets.UTF_8)
        assertTrue(csv.contains("מונה"))
        assertTrue(csv.contains("ערך"))
    }

    private fun writeZipBytes(data: AllExportData): ByteArray {
        val output = ByteArrayOutputStream()
        AllDataZipExporter.writeZip(
            outputStream = output,
            data = data,
            invoiceLabels = testInvoiceLabels(),
            categoryLabels = testCategoryLabels()
        )
        return output.toByteArray()
    }

    private fun listZipEntryNames(zipBytes: ByteArray): List<String> {
        val entryNames = mutableListOf<String>()
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return entryNames
    }

    private fun readZipEntryBytes(zipBytes: ByteArray, entryName: String): ByteArray {
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == entryName) {
                    return zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        throw IllegalArgumentException("ZIP entry not found: $entryName")
    }

    private fun readZipEntryText(zipBytes: ByteArray, entryName: String): String {
        val bytes = readZipEntryBytes(zipBytes, entryName)
        val content = if (
            bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            bytes.copyOfRange(3, bytes.size)
        } else {
            bytes
        }
        return content.toString(Charsets.UTF_8)
    }

    private fun testCategoryLabels() = CategoriesCsvLabels(
        categoryNameHeader = "Category Name",
        descriptionHeader = "Description",
        orderHeader = "Order",
        customFieldTitleHeader = { "Custom Field $it Title" }
    )

    private fun testInvoiceLabels() = InvoiceCsvExportLabels(
        categoryNameHeader = "Category Name",
        invoiceNumberHeader = "Invoice Number",
        amountHeader = "Amount",
        currencyHeader = "Currency",
        statusHeader = "Status",
        servicePeriodModeHeader = "Service Period Mode",
        servicePeriodStartHeader = "Service Period Start",
        servicePeriodEndHeader = "Service Period End",
        dueDateHeader = "Due Date",
        paymentDateHeader = "Payment Date",
        paymentMethodHeader = "Payment Method",
        confirmationNumberHeader = "Confirmation Number",
        notesHeader = "Notes",
        customFieldFallbackHeader = { "Custom Field $it" },
        paymentStatusPaid = "Paid",
        paymentStatusNotPaid = "Not paid",
        servicePeriodModeMonth = "Month",
        servicePeriodModeDate = "Date",
        currencyIls = "ILS",
        currencyUsd = "USD",
        paymentMethodByStoredValue = emptyMap(),
        formatDate = { it?.toString().orEmpty() }
    )
}
