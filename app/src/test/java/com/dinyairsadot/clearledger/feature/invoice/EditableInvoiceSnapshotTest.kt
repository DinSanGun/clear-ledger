package com.dinyairsadot.clearledger.feature.invoice

import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency
import com.dinyairsadot.clearledger.core.domain.PaymentMethodOption
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EditableInvoiceSnapshotTest {

    private fun untouchedAddInvoiceSnapshot(
        year: Int = 2026,
        month: Int = 7
    ) = editableInvoiceSnapshot(
        documentNumber = "",
        amount = "",
        currencyCode = InvoiceCurrency.ILS.name,
        paymentStatus = PaymentStatus.NOT_PAID,
        servicePeriodMode = ServicePeriodMode.MONTH,
        servicePeriodStartText = "",
        servicePeriodEndText = "",
        startYear = year,
        startMonth = month,
        showEndMonth = false,
        endYear = year,
        endMonth = month,
        paymentDateText = "",
        dueDateText = "",
        paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
        paymentMethodOtherText = "",
        numberOfPayments = "",
        confirmationNumber = "",
        vendorName = "",
        notes = "",
        customFieldValues = emptyList()
    )

    @Test
    fun `untouched add invoice form with defaults is not dirty`() {
        val original = untouchedAddInvoiceSnapshot()
        val current = untouchedAddInvoiceSnapshot()
        assertEquals(original, current)
    }

    @Test
    fun `editing one invoice value makes form dirty`() {
        val original = untouchedAddInvoiceSnapshot()
        val current = editableInvoiceSnapshot(
            documentNumber = "INV-1",
            amount = "",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = "",
            servicePeriodEndText = "",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "",
            dueDateText = "",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "",
            numberOfPayments = "",
            confirmationNumber = "",
            vendorName = "",
            notes = "",
            customFieldValues = emptyList()
        )
        assertNotEquals(original, current)
    }

    @Test
    fun `restoring invoice values removes dirty state`() {
        val original = untouchedAddInvoiceSnapshot()
        val edited = editableInvoiceSnapshot(
            documentNumber = "INV-1",
            amount = "100",
            currencyCode = InvoiceCurrency.USD.name,
            paymentStatus = PaymentStatus.PAID,
            servicePeriodMode = ServicePeriodMode.DATE,
            servicePeriodStartText = "01/01/2026",
            servicePeriodEndText = "31/01/2026",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = true,
            endYear = 2026,
            endMonth = 8,
            paymentDateText = "15/01/2026",
            dueDateText = "",
            paymentMethod = PaymentMethodOption.CASH.value,
            paymentMethodOtherText = "",
            numberOfPayments = "1",
            confirmationNumber = "ABC",
            vendorName = "Vendor",
            notes = "Note",
            customFieldValues = listOf("x")
        )
        assertNotEquals(original, edited)
        assertEquals(original, untouchedAddInvoiceSnapshot())
    }

    @Test
    fun `untouched edit invoice form is not dirty`() {
        val original = editableInvoiceSnapshot(
            documentNumber = "100",
            amount = "50.00",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = "01/07/2026",
            servicePeriodEndText = "31/07/2026",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "",
            dueDateText = "10/08/2026",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "",
            numberOfPayments = "",
            confirmationNumber = "",
            vendorName = "City",
            notes = "",
            customFieldValues = listOf("A")
        )
        val current = editableInvoiceSnapshot(
            documentNumber = "100",
            amount = "50.00",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = "01/07/2026",
            servicePeriodEndText = "31/07/2026",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "",
            dueDateText = "10/08/2026",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "",
            numberOfPayments = "",
            confirmationNumber = "",
            vendorName = "City",
            notes = "",
            customFieldValues = listOf("A")
        )
        assertEquals(original, current)
    }

    @Test
    fun `whitespace only invoice text fields are not dirty`() {
        val original = editableInvoiceSnapshot(
            documentNumber = "",
            amount = "",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = "",
            servicePeriodEndText = "",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "",
            dueDateText = "",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "",
            numberOfPayments = "",
            confirmationNumber = "",
            vendorName = "",
            notes = "",
            customFieldValues = listOf("")
        )
        val current = editableInvoiceSnapshot(
            documentNumber = "  ",
            amount = "\t",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = " ",
            servicePeriodEndText = " ",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "  ",
            dueDateText = "  ",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "  ",
            numberOfPayments = " ",
            confirmationNumber = " ",
            vendorName = "  ",
            notes = "  ",
            customFieldValues = listOf("  ")
        )
        assertEquals(original, current)
    }

    @Test
    fun `changing and restoring amount removes dirty state`() {
        val original = untouchedAddInvoiceSnapshot()
        val changed = editableInvoiceSnapshot(
            documentNumber = "",
            amount = "12.5",
            currencyCode = InvoiceCurrency.ILS.name,
            paymentStatus = PaymentStatus.NOT_PAID,
            servicePeriodMode = ServicePeriodMode.MONTH,
            servicePeriodStartText = "",
            servicePeriodEndText = "",
            startYear = 2026,
            startMonth = 7,
            showEndMonth = false,
            endYear = 2026,
            endMonth = 7,
            paymentDateText = "",
            dueDateText = "",
            paymentMethod = PaymentMethodOption.NOT_SPECIFIED.value,
            paymentMethodOtherText = "",
            numberOfPayments = "",
            confirmationNumber = "",
            vendorName = "",
            notes = "",
            customFieldValues = emptyList()
        )
        assertNotEquals(original, changed)
        assertEquals(original, untouchedAddInvoiceSnapshot())
    }
}
