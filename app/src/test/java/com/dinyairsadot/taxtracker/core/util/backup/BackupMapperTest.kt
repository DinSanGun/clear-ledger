package com.dinyairsadot.taxtracker.core.util.backup

import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.DocumentType
import com.dinyairsadot.taxtracker.core.domain.Invoice
import com.dinyairsadot.taxtracker.core.domain.InvoiceCurrency
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BackupMapperTest {

    @Test
    fun fromCategoryDto_roundTrip_preservesCategory() {
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

        val dto = BackupMapper.toCategoryDto(category)
        val restored = BackupMapper.fromCategoryDto(dto)

        assertEquals(category, restored)
    }

    @Test
    fun fromInvoiceDto_roundTrip_preservesInvoice() {
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

        val dto = BackupMapper.toInvoiceDto(invoice)
        val restored = BackupMapper.fromInvoiceDto(dto)

        assertEquals(invoice, restored)
    }

    @Test
    fun fromInvoiceDto_nullableFields_preservedAsNull() {
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

        val dto = BackupMapper.toInvoiceDto(invoice)
        val restored = BackupMapper.fromInvoiceDto(dto)

        assertNull(restored.issueDate)
        assertNull(restored.dueDate)
        assertNull(restored.documentType)
        assertNull(restored.vendorName)
    }

    @Test
    fun fromInvoiceDto_servicePeriodModeDate_preservedExactly() {
        val invoice = Invoice(
            id = 1L,
            categoryId = 1L,
            invoiceNumber = "INV",
            amount = 10.0,
            paymentStatus = PaymentStatus.PAID,
            servicePeriodMode = ServicePeriodMode.DATE
        )

        val dto = BackupMapper.toInvoiceDto(invoice)
        val restored = BackupMapper.fromInvoiceDto(dto)

        assertEquals(ServicePeriodMode.DATE, restored.servicePeriodMode)
    }

    @Test
    fun customFieldAlignment_blankMiddleValue_roundTripsByIndex() {
        val category = Category(
            id = 1L,
            name = "Test",
            colorHex = "#000000",
            customFieldTitles = listOf("T1", "T2", "T3"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 50.0,
            paymentStatus = PaymentStatus.PAID,
            customFieldValues = listOf("a", "", "c")
        )

        val restoredCategory = BackupMapper.fromCategoryDto(BackupMapper.toCategoryDto(category))
        val restoredInvoice = BackupMapper.fromInvoiceDto(BackupMapper.toInvoiceDto(invoice))

        assertEquals(category, restoredCategory)
        assertEquals(invoice, restoredInvoice)
        assertEquals(listOf("a", "", "c"), restoredInvoice.customFieldValues)
    }

    @Test
    fun customFieldAlignment_shorterValuesList_roundTripsWithoutPadding() {
        val category = Category(
            id = 1L,
            name = "Test",
            colorHex = "#000000",
            customFieldTitles = listOf("T1", "T2", "T3"),
            orderIndex = 0
        )
        val invoice = Invoice(
            id = 10L,
            categoryId = 1L,
            invoiceNumber = "INV-1",
            amount = 50.0,
            paymentStatus = PaymentStatus.PAID,
            customFieldValues = listOf("x")
        )

        val restoredInvoice = BackupMapper.fromInvoiceDto(BackupMapper.toInvoiceDto(invoice))

        assertEquals(listOf("x"), restoredInvoice.customFieldValues)
    }
}
