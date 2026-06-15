package com.dinyairsadot.clearledger.core.util

import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency
import com.dinyairsadot.clearledger.core.domain.PaymentMethodOption
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InvoiceCsvExporterTest {

    private val englishLabels = InvoiceCsvExportLabels(
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
        customFieldFallbackHeader = { index -> "Custom Field $index" },
        paymentStatusPaid = "Paid",
        paymentStatusNotPaid = "Not paid",
        servicePeriodModeMonth = "By Month",
        servicePeriodModeDate = "Exact dates",
        currencyIls = "ILS",
        currencyUsd = "USD",
        paymentMethodByStoredValue = mapOf(
            PaymentMethodOption.CREDIT.value to "Credit Card",
            PaymentMethodOption.BANK_TRANSFER.value to "Bank Transfer"
        ),
        formatDate = { date -> date?.toString().orEmpty() }
    )

    private val hebrewLabels = englishLabels.copy(
        categoryNameHeader = "שם קטגוריה",
        invoiceNumberHeader = "מספר חשבונית",
        statusHeader = "סטטוס",
        paymentStatusPaid = "שולם",
        paymentStatusNotPaid = "לא שולם",
        servicePeriodModeMonth = "חודשית",
        servicePeriodModeDate = "תאריכים מדויקים",
        currencyIls = "ש״ח (₪)",
        customFieldFallbackHeader = { index -> "שדה מותאם $index" }
    )

    @Test
    fun generate_usesCustomFieldTitleAsColumnHeader() {
        val csv = InvoiceCsvExporter.generate(
            invoices = emptyList(),
            categoryName = "Arnona",
            customFieldTitles = listOf("Supplier Number", "PO Number"),
            labels = englishLabels
        )
        assertTrue(csv.startsWith("Category Name,Invoice Number"))
        assertTrue(csv.endsWith("Supplier Number,PO Number"))
        assertFalse(csv.contains("Custom Field 1 Title"))
        assertFalse(csv.contains("Custom Field 1 Value"))
    }

    @Test
    fun generate_blankCustomFieldTitleUsesLocalizedFallbackHeader() {
        val csv = InvoiceCsvExporter.generate(
            invoices = emptyList(),
            categoryName = "Test",
            customFieldTitles = listOf("", "  "),
            labels = englishLabels
        )
        assertTrue(csv.endsWith("Custom Field 1,Custom Field 2"))
    }

    @Test
    fun generate_customFieldValueUnderTitleHeaderOnly() {
        val invoice = sampleInvoice(
            paymentMethod = PaymentMethodOption.CREDIT.value,
            customFieldValues = listOf("12345", "value-2")
        )
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Utilities",
            customFieldTitles = listOf("Supplier Number", "Meter ID"),
            labels = englishLabels
        )
        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].endsWith("Supplier Number,Meter ID"))
        assertTrue(lines[1].endsWith("12345,value-2"))
        assertFalse(lines[1].contains("Custom Field"))
    }

    @Test
    fun generate_englishCurrencyUsesAsciiIlsAndUsd() {
        val invoice = sampleInvoice()
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Utilities",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        assertTrue(csv.contains(",ILS,"))
        assertFalse(csv.contains("₪"))
        assertFalse(csv.contains("$"))
    }

    @Test
    fun generate_hebrewExportPreservesHebrewText() {
        val invoice = sampleInvoice(customFieldValues = listOf("ערך"))
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "ארנונה",
            customFieldTitles = listOf("מונה"),
            labels = hebrewLabels
        )
        assertTrue(Utf8CsvWriter.containsNonAscii(csv))
        assertTrue(csv.contains("שם קטגוריה"))
        assertTrue(csv.contains("שולם"))
        assertTrue(csv.contains("ש״ח (₪)"))
        assertTrue(csv.contains("מונה"))
        assertTrue(csv.contains("ערך"))
    }

    @Test
    fun generate_hebrewCustomFieldTitleAsHeader() {
        val invoice = sampleInvoice(customFieldValues = listOf("ערך"))
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "ארנונה",
            customFieldTitles = listOf("מונה"),
            labels = hebrewLabels
        )
        val lines = csv.lines()
        assertTrue(lines[0].endsWith("מונה"))
        assertTrue(lines[1].endsWith("ערך"))
        assertTrue(csv.contains("שולם"))
        assertFalse(csv.contains("Custom Field 1"))
    }

    @Test
    fun generate_escapesCommasInCustomFieldTitleAndValue() {
        val invoice = sampleInvoice(customFieldValues = listOf("a,b"))
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Test",
            customFieldTitles = listOf("Supplier, Name"),
            labels = englishLabels
        )
        assertTrue(csv.contains("\"Supplier, Name\""))
        assertTrue(csv.contains("\"a,b\""))
    }

    @Test
    fun generate_escapesCommasAndQuotesInBuiltInFields() {
        val invoice = sampleInvoice(
            invoiceNumber = "INV,1",
            notes = "Line \"quoted\"\nsecond line"
        )
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Test",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        assertTrue(csv.contains("\"INV,1\""))
        assertTrue(csv.contains("\"Line \"\"quoted\"\"\nsecond line\""))
    }

    @Test
    fun escapeCsvField_wrapsWhenNeeded() {
        assertEquals("plain", InvoiceCsvExporter.escapeCsvField("plain"))
        assertEquals("\"a,b\"", InvoiceCsvExporter.escapeCsvField("a,b"))
        assertEquals("\"say \"\"hi\"\"\"", InvoiceCsvExporter.escapeCsvField("say \"hi\""))
    }

    @Test
    fun generate_shorterCustomFieldValues_padsEmptyCellsByIndex() {
        val invoice = sampleInvoice(customFieldValues = listOf("v1"))
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Test",
            customFieldTitles = listOf("A", "B", "C"),
            labels = englishLabels
        )
        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].endsWith("A,B,C"))
        assertTrue(lines[1].endsWith("v1,,"))
    }

    @Test
    fun generate_blankMiddleCustomFieldValue_preservesColumnPosition() {
        val invoice = sampleInvoice(customFieldValues = listOf("v1", "", "v3"))
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Test",
            customFieldTitles = listOf("A", "B", "C"),
            labels = englishLabels
        )
        val lines = csv.lines()
        assertTrue(lines[0].endsWith("A,B,C"))
        assertTrue(lines[1].endsWith("v1,,v3"))
    }

    @Test
    fun generate_exportsOnlyInvoicesInInputList() {
        val first = sampleInvoice(invoiceNumber = "INV-1")
        val second = sampleInvoice(invoiceNumber = "INV-2")
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(first),
            categoryName = "Utilities",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[1].contains("INV-1"))
        assertFalse(csv.contains("INV-2"))
    }

    @Test
    fun generate_usdCurrencyUsesAsciiUsdLabel() {
        val invoice = sampleInvoice(amountCurrency = InvoiceCurrency.USD)
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Utilities",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        assertTrue(csv.contains(",USD,"))
    }

    @Test
    fun generate_amountFormatting_stripsTrailingZeros() {
        val whole = sampleInvoice(invoiceNumber = "INV-W", amount = 100.0)
        val decimal = sampleInvoice(invoiceNumber = "INV-D", amount = 123.45)
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(whole, decimal),
            categoryName = "Utilities",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        val lines = csv.lines()
        assertEquals(3, lines.size)
        assertTrue(lines[1].contains(",100,"))
        assertTrue(lines[2].contains(",123.45,"))
    }

    @Test
    fun generate_servicePeriodModeAndDatesInRow() {
        val invoice = sampleInvoice(
            servicePeriodMode = ServicePeriodMode.DATE,
            servicePeriodStart = LocalDate.of(2026, 1, 1),
            servicePeriodEnd = LocalDate.of(2026, 1, 31),
            dueDate = LocalDate.of(2026, 2, 15),
            paymentDate = LocalDate.of(2026, 2, 10)
        )
        val csv = InvoiceCsvExporter.generate(
            invoices = listOf(invoice),
            categoryName = "Utilities",
            customFieldTitles = emptyList(),
            labels = englishLabels
        )
        val dataRow = csv.lines()[1]
        assertTrue(dataRow.contains("Exact dates"))
        assertTrue(dataRow.contains("2026-01-01"))
        assertTrue(dataRow.contains("2026-01-31"))
        assertTrue(dataRow.contains("2026-02-15"))
        assertTrue(dataRow.contains("2026-02-10"))
    }

    private fun sampleInvoice(
        invoiceNumber: String = "100",
        amount: Double = 123.45,
        notes: String? = null,
        paymentMethod: String? = null,
        customFieldValues: List<String> = emptyList(),
        amountCurrency: InvoiceCurrency = InvoiceCurrency.ILS,
        servicePeriodMode: ServicePeriodMode = ServicePeriodMode.MONTH,
        servicePeriodStart: LocalDate? = null,
        servicePeriodEnd: LocalDate? = null,
        dueDate: LocalDate? = LocalDate.of(2026, 1, 15),
        paymentDate: LocalDate? = null
    ): Invoice = Invoice(
        id = 1L,
        categoryId = 10L,
        invoiceNumber = invoiceNumber,
        amount = amount,
        paymentStatus = PaymentStatus.PAID,
        dueDate = dueDate,
        paymentDate = paymentDate,
        servicePeriodStart = servicePeriodStart,
        servicePeriodEnd = servicePeriodEnd,
        notes = notes,
        paymentMethod = paymentMethod,
        customFieldValues = customFieldValues,
        servicePeriodMode = servicePeriodMode,
        amountCurrency = amountCurrency
    )
}
